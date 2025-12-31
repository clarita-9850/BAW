package com.cmips.integration.framework.baw.repository;

import com.cmips.integration.framework.baw.annotation.FileId;
import com.cmips.integration.framework.baw.annotation.FileType;
import com.cmips.integration.framework.baw.annotation.MapsFrom;
import com.cmips.integration.framework.baw.exception.ConversionException;
import com.cmips.integration.framework.baw.exception.FileParseException;
import com.cmips.integration.framework.baw.exception.FileWriteException;
import com.cmips.integration.framework.baw.exception.SchemaValidationException;
import com.cmips.integration.framework.baw.format.FileFormat;
import com.cmips.integration.framework.baw.split.SplitResult;
import com.cmips.integration.framework.baw.split.SplitRule;
import com.cmips.integration.framework.baw.validation.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of FileRepository.
 *
 * @param <T> the file type
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class DefaultFileRepository<T> implements FileRepository<T> {

    private static final Logger log = LoggerFactory.getLogger(DefaultFileRepository.class);

    private final Class<T> type;
    private final FileType fileTypeAnnotation;
    private final Schema schema;
    private final ObjectMapper jsonMapper;
    private final XmlMapper xmlMapper;
    private final List<Field> idFields;

    public DefaultFileRepository(Class<T> type) {
        this.type = type;
        this.fileTypeAnnotation = type.getAnnotation(FileType.class);

        if (fileTypeAnnotation == null) {
            throw new SchemaValidationException(type, "Missing @FileType annotation");
        }

        this.schema = Schema.from(type);
        this.idFields = findIdFields();

        // Initialize mappers
        this.jsonMapper = new ObjectMapper();
        this.jsonMapper.registerModule(new JavaTimeModule());
        this.jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        this.xmlMapper = new XmlMapper();
        this.xmlMapper.registerModule(new JavaTimeModule());
        this.xmlMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        log.debug("Created repository for file type: {}", fileTypeAnnotation.name());
    }

    private List<Field> findIdFields() {
        List<Field> fields = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(FileId.class)) {
                field.setAccessible(true);
                fields.add(field);
            }
        }
        fields.sort(Comparator.comparingInt(f -> {
            FileId id = f.getAnnotation(FileId.class);
            return id != null ? id.order() : 0;
        }));
        return fields;
    }

    // ========== Read Operations ==========

    @Override
    public List<T> read(Path path, FileFormat format) throws FileParseException {
        try {
            log.debug("Reading {} from {}", fileTypeAnnotation.name(), path);
            String content = Files.readString(path, format.getCharset());
            return parseContent(content, format);
        } catch (Exception e) {
            throw new FileParseException("Failed to read file: " + path, e);
        }
    }

    @Override
    public List<T> read(InputStream stream, FileFormat format) throws FileParseException {
        try {
            String content = new String(stream.readAllBytes(), format.getCharset());
            return parseContent(content, format);
        } catch (Exception e) {
            throw new FileParseException("Failed to read from stream", e);
        }
    }

    @Override
    public List<T> readAll(List<Path> paths, FileFormat format) throws FileParseException {
        List<T> all = new ArrayList<>();
        for (Path path : paths) {
            all.addAll(read(path, format));
        }
        return all;
    }

    @Override
    public Stream<T> readStream(Path path, FileFormat format) throws FileParseException {
        // For now, load all into memory. Could be optimized for large files.
        return read(path, format).stream();
    }

    @SuppressWarnings("unchecked")
    private List<T> parseContent(String content, FileFormat format) throws Exception {
        switch (format.getType()) {
            case JSON:
            case JSON_LINES:
                return Arrays.asList(jsonMapper.readValue(content,
                        jsonMapper.getTypeFactory().constructArrayType(type)));
            case XML:
                // For XML, we need to handle the wrapper element
                return Arrays.asList(xmlMapper.readValue(content,
                        xmlMapper.getTypeFactory().constructArrayType(type)));
            case CSV:
            case TSV:
            case PIPE:
                return parseCsv(content, format);
            case FIXED_WIDTH:
                return parseFixedWidth(content, format);
            default:
                throw new FileParseException("Unsupported format: " + format.getType());
        }
    }

    private List<T> parseCsv(String content, FileFormat format) throws Exception {
        List<T> records = new ArrayList<>();
        String[] lines = content.split(format.getLineSeparator().isEmpty() ? "\\R" : format.getLineSeparator());

        if (lines.length == 0) {
            return records;
        }

        String delimiter = String.valueOf(format.getDelimiter());
        String[] headers = null;

        int startLine = 0;
        if (format.hasHeader()) {
            headers = lines[0].split(delimiter);
            startLine = 1;
        }

        for (int i = startLine; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] values = line.split(delimiter, -1);
            T record = type.getDeclaredConstructor().newInstance();

            for (Schema.ColumnSchema col : schema.getColumns()) {
                int idx = col.getOrder() - 1;
                if (idx < values.length) {
                    Field field = type.getDeclaredField(col.getFieldName());
                    field.setAccessible(true);
                    Object value = convertValue(values[idx].trim(), field.getType());
                    field.set(record, value);
                }
            }
            records.add(record);
        }
        return records;
    }

    private List<T> parseFixedWidth(String content, FileFormat format) throws Exception {
        List<T> records = new ArrayList<>();
        String[] lines = content.split(format.getLineSeparator().isEmpty() ? "\\R" : format.getLineSeparator());

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            T record = type.getDeclaredConstructor().newInstance();
            int pos = 0;

            for (Schema.ColumnSchema col : schema.getColumns()) {
                if (col.getLength() > 0 && pos + col.getLength() <= line.length()) {
                    String value = line.substring(pos, pos + col.getLength()).trim();
                    Field field = type.getDeclaredField(col.getFieldName());
                    field.setAccessible(true);
                    field.set(record, convertValue(value, field.getType()));
                    pos += col.getLength();
                }
            }
            records.add(record);
        }
        return records;
    }

    private Object convertValue(String value, Class<?> targetType) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        if (targetType == String.class) return value;
        if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(value);
        if (targetType == Long.class || targetType == long.class) return Long.parseLong(value);
        if (targetType == Double.class || targetType == double.class) return Double.parseDouble(value);
        if (targetType == Boolean.class || targetType == boolean.class) return Boolean.parseBoolean(value);
        if (targetType == java.math.BigDecimal.class) return new java.math.BigDecimal(value);

        // Date/Time types
        if (targetType == LocalDate.class) {
            return parseLocalDate(value);
        }
        if (targetType == LocalDateTime.class) {
            return parseLocalDateTime(value);
        }
        if (targetType == Instant.class) {
            return Instant.parse(value);
        }

        return value;
    }

    private LocalDate parseLocalDate(String value) {
        // Try common date formats
        String[] patterns = {"yyyy-MM-dd", "MM/dd/yyyy", "dd-MM-yyyy", "yyyy/MM/dd"};
        for (String pattern : patterns) {
            try {
                return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException e) {
                // Try next pattern
            }
        }
        // Try ISO format as fallback
        return LocalDate.parse(value);
    }

    private LocalDateTime parseLocalDateTime(String value) {
        // Try common datetime formats
        String[] patterns = {
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "MM/dd/yyyy HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS"
        };
        for (String pattern : patterns) {
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException e) {
                // Try next pattern
            }
        }
        // Try ISO format as fallback
        return LocalDateTime.parse(value);
    }

    // ========== Write Operations ==========

    @Override
    public void write(List<T> records, Path path, FileFormat format) throws FileWriteException {
        try {
            log.debug("Writing {} records to {}", records.size(), path);
            Files.createDirectories(path.getParent());
            byte[] content = writeToBytes(records, format);
            Files.write(path, content);
        } catch (Exception e) {
            throw new FileWriteException(path, "Failed to write file", e);
        }
    }

    @Override
    public void write(List<T> records, OutputStream stream, FileFormat format) throws FileWriteException {
        try {
            byte[] content = writeToBytes(records, format);
            stream.write(content);
        } catch (Exception e) {
            throw new FileWriteException("Failed to write to stream", e);
        }
    }

    @Override
    public byte[] writeToBytes(List<T> records, FileFormat format) throws FileWriteException {
        try {
            switch (format.getType()) {
                case JSON:
                    if (format.isPrettyPrint()) {
                        return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(records);
                    }
                    return jsonMapper.writeValueAsBytes(records);
                case JSON_LINES:
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    for (T record : records) {
                        baos.write(jsonMapper.writeValueAsBytes(record));
                        baos.write('\n');
                    }
                    return baos.toByteArray();
                case XML:
                    return xmlMapper.writeValueAsBytes(records);
                case CSV:
                case TSV:
                case PIPE:
                    return writeCsv(records, format);
                case FIXED_WIDTH:
                    return writeFixedWidth(records, format);
                default:
                    throw new FileWriteException("Unsupported format: " + format.getType());
            }
        } catch (Exception e) {
            throw new FileWriteException("Failed to write records", e);
        }
    }

    private byte[] writeCsv(List<T> records, FileFormat format) throws Exception {
        StringBuilder sb = new StringBuilder();
        String delimiter = String.valueOf(format.getDelimiter());

        // Write header
        if (format.hasHeader()) {
            sb.append(schema.getColumns().stream()
                    .map(Schema.ColumnSchema::getName)
                    .collect(Collectors.joining(delimiter)));
            sb.append(format.getLineSeparator());
        }

        // Write records
        for (T record : records) {
            List<String> values = new ArrayList<>();
            for (Schema.ColumnSchema col : schema.getColumns()) {
                Field field = type.getDeclaredField(col.getFieldName());
                field.setAccessible(true);
                Object value = field.get(record);
                values.add(value != null ? value.toString() : format.getNullValue());
            }
            sb.append(String.join(delimiter, values));
            sb.append(format.getLineSeparator());
        }

        return sb.toString().getBytes(format.getCharset());
    }

    private byte[] writeFixedWidth(List<T> records, FileFormat format) throws Exception {
        StringBuilder sb = new StringBuilder();

        for (T record : records) {
            for (Schema.ColumnSchema col : schema.getColumns()) {
                Field field = type.getDeclaredField(col.getFieldName());
                field.setAccessible(true);
                Object value = field.get(record);
                String strValue = value != null ? value.toString() : "";

                if (col.getLength() > 0) {
                    strValue = padOrTruncate(strValue, col.getLength());
                }
                sb.append(strValue);
            }
            sb.append(format.getLineSeparator());
        }

        return sb.toString().getBytes(format.getCharset());
    }

    private String padOrTruncate(String value, int length) {
        if (value.length() > length) {
            return value.substring(0, length);
        }
        return String.format("%-" + length + "s", value);
    }

    // ========== Merge Operations ==========

    @Override
    public MergeBuilder<T> merge(List<T> first, List<T> second) {
        return new DefaultMergeBuilder<>(Arrays.asList(first, second), this);
    }

    @Override
    @SafeVarargs
    public final MergeBuilder<T> merge(List<T>... lists) {
        return new DefaultMergeBuilder<>(Arrays.asList(lists), this);
    }

    Function<T, Object> getIdExtractor() {
        return record -> {
            if (idFields.isEmpty()) {
                return record;
            }
            try {
                if (idFields.size() == 1) {
                    return idFields.get(0).get(record);
                }
                // Composite key
                List<Object> keyParts = new ArrayList<>();
                for (Field f : idFields) {
                    keyParts.add(f.get(record));
                }
                return keyParts;
            } catch (Exception e) {
                throw new RuntimeException("Failed to extract ID", e);
            }
        };
    }

    // ========== Split Operations ==========

    @Override
    public SplitResult<T> split(List<T> records, SplitRule<T> rule) {
        return SplitResult.split(records, rule);
    }

    // ========== Convert Operations ==========

    @Override
    public <R> List<R> convert(List<T> records, Class<R> targetType) {
        try {
            List<R> results = new ArrayList<>();
            for (T source : records) {
                R target = convertRecord(source, targetType);
                results.add(target);
            }
            return results;
        } catch (Exception e) {
            throw new ConversionException(type, targetType, e.getMessage());
        }
    }

    private <R> R convertRecord(T source, Class<R> targetType) throws Exception {
        R target = targetType.getDeclaredConstructor().newInstance();

        for (Field targetField : targetType.getDeclaredFields()) {
            MapsFrom mapping = targetField.getAnnotation(MapsFrom.class);
            if (mapping != null && mapping.source() == type) {
                Field sourceField = type.getDeclaredField(mapping.field());
                sourceField.setAccessible(true);
                targetField.setAccessible(true);

                Object value = sourceField.get(source);

                // Apply transformer if specified
                if (mapping.transformer() != MapsFrom.NoOpTransformer.class) {
                    var transformer = mapping.transformer().getDeclaredConstructor().newInstance();
                    value = ((com.cmips.integration.framework.baw.annotation.FieldTransformer) transformer).transform(value);
                }

                targetField.set(target, value);
            }
        }

        return target;
    }

    // ========== Query Operations ==========

    @Override
    public List<T> findAll(List<T> records, Predicate<T> predicate) {
        return records.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<T> findFirst(List<T> records, Predicate<T> predicate) {
        return records.stream()
                .filter(predicate)
                .findFirst();
    }

    @Override
    public long count(List<T> records, Predicate<T> predicate) {
        return records.stream()
                .filter(predicate)
                .count();
    }

    // ========== Validation Operations ==========

    @Override
    public ValidationResult<T> validate(List<T> records) {
        ValidationResult.Builder<T> builder = ValidationResult.forType(type);

        for (T record : records) {
            // Basic schema validation
            boolean valid = true;
            for (Schema.ColumnSchema col : schema.getColumns()) {
                if (!col.isNullable()) {
                    try {
                        Field field = type.getDeclaredField(col.getFieldName());
                        field.setAccessible(true);
                        if (field.get(record) == null) {
                            builder.addInvalid(record, new com.cmips.integration.framework.baw.annotation.ValidationError(
                                    col.getFieldName(), "Field is required", null
                            ));
                            valid = false;
                            break;
                        }
                    } catch (Exception e) {
                        // Skip
                    }
                }
            }
            if (valid) {
                builder.addValid(record);
            }
        }

        return builder.build();
    }

    // ========== Send Operations ==========

    @Override
    public SendBuilder<T> send(List<T> records) {
        return new DefaultSendBuilder<>(records, this);
    }

    // ========== Metadata Operations ==========

    @Override
    public FileType getFileType() {
        return fileTypeAnnotation;
    }

    @Override
    public Schema getSchema() {
        return schema;
    }
}
