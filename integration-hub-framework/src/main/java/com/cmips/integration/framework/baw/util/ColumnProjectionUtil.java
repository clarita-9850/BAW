package com.cmips.integration.framework.baw.util;

import com.cmips.integration.framework.baw.annotation.FileColumn;
import com.cmips.integration.framework.baw.exception.FileWriteException;
import com.cmips.integration.framework.baw.format.FileFormat;
import com.cmips.integration.framework.baw.repository.FileRepository;
import com.cmips.integration.framework.baw.repository.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for writing files with column projection (selecting specific columns).
 * 
 * <p>This utility allows writing records to files with only a subset of columns,
 * which is useful for splitting files by columns or creating views with specific fields.
 * 
 * <p>Example usage:
 * <pre>
 * FileRepository&lt;Employee&gt; repo = FileRepository.forType(Employee.class);
 * List&lt;Employee&gt; records = repo.read(inputPath, FileFormat.csv());
 * 
 * // Write only id, name, department columns
 * ColumnProjectionUtil.writeWithColumns(
 *     repo,
 *     records,
 *     outputPath,
 *     FileFormat.csv().build(),
 *     Arrays.asList("id", "name", "department")
 * );
 * </pre>
 * 
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ColumnProjectionUtil {

    private static final Logger log = LoggerFactory.getLogger(ColumnProjectionUtil.class);
    
    private static final ObjectMapper jsonMapper;
    private static final XmlMapper xmlMapper;
    
    static {
        jsonMapper = new ObjectMapper();
        jsonMapper.registerModule(new JavaTimeModule());
        jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private ColumnProjectionUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Writes records to a file with only selected columns.
     * 
     * @param <T> the record type
     * @param repository the FileRepository instance
     * @param records the records to write
     * @param path the output file path
     * @param format the file format
     * @param selectedColumns list of column names to include (must match @FileColumn name or field name)
     * @throws FileWriteException if writing fails
     */
    public static <T> void writeWithColumns(
            FileRepository<T> repository,
            List<T> records,
            Path path,
            FileFormat format,
            List<String> selectedColumns) throws FileWriteException {
        try {
            log.debug("Writing {} records to {} with {} selected columns", 
                    records.size(), path, selectedColumns.size());
            Files.createDirectories(path.getParent());
            byte[] content = writeToBytesWithColumns(repository, records, format, selectedColumns);
            Files.write(path, content);
        } catch (Exception e) {
            throw new FileWriteException(path, "Failed to write file with column projection", e);
        }
    }

    /**
     * Writes records to bytes with only selected columns.
     * 
     * @param <T> the record type
     * @param repository the FileRepository instance
     * @param records the records to write
     * @param format the file format
     * @param selectedColumns list of column names to include
     * @return the content as bytes
     * @throws FileWriteException if writing fails
     */
    public static <T> byte[] writeToBytesWithColumns(
            FileRepository<T> repository,
            List<T> records,
            FileFormat format,
            List<String> selectedColumns) throws FileWriteException {
        try {
            // Get schema from repository
            Schema schema = repository.getSchema();
            Class<?> recordType = schema.getType();
            
            // Validate and filter columns
            List<Schema.ColumnSchema> filteredColumns = validateAndFilterColumns(
                    schema, selectedColumns, recordType);
            
            // Write based on format type
            switch (format.getType()) {
                case CSV:
                case TSV:
                case PIPE:
                    return writeCsvWithColumns(records, format, filteredColumns, recordType);
                case JSON:
                case JSON_LINES:
                    return writeJsonWithColumns(records, format, filteredColumns, recordType);
                case XML:
                    return writeXmlWithColumns(records, format, filteredColumns, recordType);
                case FIXED_WIDTH:
                    return writeFixedWidthWithColumns(records, format, filteredColumns, recordType);
                default:
                    throw new FileWriteException("Unsupported format for column projection: " + format.getType());
            }
        } catch (FileWriteException e) {
            throw e;
        } catch (Exception e) {
            throw new FileWriteException("Failed to write records with column projection", e);
        }
    }

    /**
     * Validates selected columns and returns filtered column schemas in order.
     */
    private static List<Schema.ColumnSchema> validateAndFilterColumns(
            Schema schema,
            List<String> selectedColumns,
            Class<?> recordType) {
        if (selectedColumns == null || selectedColumns.isEmpty()) {
            throw new IllegalArgumentException("Selected columns list cannot be empty");
        }
        
        List<Schema.ColumnSchema> filtered = new ArrayList<>();
        Set<String> selectedSet = new HashSet<>(selectedColumns);
        
        for (String colName : selectedColumns) {
            Schema.ColumnSchema col = schema.getColumn(colName);
            if (col == null) {
                throw new IllegalArgumentException(
                    String.format("Column '%s' not found in schema for type %s. Available columns: %s",
                        colName, recordType.getSimpleName(),
                        schema.getColumns().stream()
                            .map(Schema.ColumnSchema::getName)
                            .collect(Collectors.joining(", "))));
            }
            filtered.add(col);
        }
        
        // Sort by order to maintain column order from schema
        filtered.sort(Comparator.comparingInt(Schema.ColumnSchema::getOrder));
        
        return filtered;
    }

    /**
     * Writes CSV/TSV/PIPE format with selected columns.
     */
    private static <T> byte[] writeCsvWithColumns(
            List<T> records,
            FileFormat format,
            List<Schema.ColumnSchema> filteredColumns,
            Class<?> recordType) throws Exception {
        StringBuilder sb = new StringBuilder();
        String delimiter = String.valueOf(format.getDelimiter());
        
        // Write header
        if (format.hasHeader()) {
            sb.append(filteredColumns.stream()
                    .map(Schema.ColumnSchema::getName)
                    .collect(Collectors.joining(delimiter)));
            sb.append(format.getLineSeparator());
        }
        
        // Write records
        Map<String, Field> fieldCache = buildFieldCache(recordType, filteredColumns);
        for (T record : records) {
            List<String> values = new ArrayList<>();
            for (Schema.ColumnSchema col : filteredColumns) {
                Field field = fieldCache.get(col.getFieldName());
                if (field == null) {
                    throw new IllegalStateException("Field not found: " + col.getFieldName());
                }
                
                Object value = field.get(record);
                String strValue = formatValue(value, col, format);
                values.add(strValue);
            }
            sb.append(String.join(delimiter, values));
            sb.append(format.getLineSeparator());
        }
        
        return sb.toString().getBytes(format.getCharset());
    }

    /**
     * Writes JSON format with selected columns.
     */
    private static <T> byte[] writeJsonWithColumns(
            List<T> records,
            FileFormat format,
            List<Schema.ColumnSchema> filteredColumns,
            Class<?> recordType) throws Exception {
        Map<String, Field> fieldCache = buildFieldCache(recordType, filteredColumns);
        
        if (format.getType() == FileFormat.FormatType.JSON_LINES) {
            // JSON Lines: one JSON object per line
            StringBuilder sb = new StringBuilder();
            for (T record : records) {
                ObjectNode node = jsonMapper.createObjectNode();
                for (Schema.ColumnSchema col : filteredColumns) {
                    Field field = fieldCache.get(col.getFieldName());
                    Object value = field.get(record);
                    addValueToJsonNode(node, col.getName(), value, col);
                }
                sb.append(jsonMapper.writeValueAsString(node));
                sb.append('\n');
            }
            return sb.toString().getBytes(format.getCharset());
        } else {
            // Regular JSON array
            List<ObjectNode> nodes = new ArrayList<>();
            for (T record : records) {
                ObjectNode node = jsonMapper.createObjectNode();
                for (Schema.ColumnSchema col : filteredColumns) {
                    Field field = fieldCache.get(col.getFieldName());
                    Object value = field.get(record);
                    addValueToJsonNode(node, col.getName(), value, col);
                }
                nodes.add(node);
            }
            
            if (format.isPrettyPrint()) {
                return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(nodes);
            } else {
                return jsonMapper.writeValueAsBytes(nodes);
            }
        }
    }

    /**
     * Writes XML format with selected columns.
     */
    private static <T> byte[] writeXmlWithColumns(
            List<T> records,
            FileFormat format,
            List<Schema.ColumnSchema> filteredColumns,
            Class<?> recordType) throws Exception {
        Map<String, Field> fieldCache = buildFieldCache(recordType, filteredColumns);
        
        StringBuilder xml = new StringBuilder();
        String rootElement = format.getRootElement() != null ? format.getRootElement() : "records";
        String recordElement = format.getRecordElement() != null ? format.getRecordElement() : "record";
        
        if (format.includeXmlDeclaration()) {
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        }
        xml.append("<").append(rootElement).append(">\n");
        
        for (T record : records) {
            xml.append("  <").append(recordElement).append(">\n");
            for (Schema.ColumnSchema col : filteredColumns) {
                Field field = fieldCache.get(col.getFieldName());
                Object value = field.get(record);
                String strValue = formatValue(value, col, format);
                
                // Escape XML special characters
                strValue = escapeXml(strValue);
                xml.append("    <").append(col.getName()).append(">")
                   .append(strValue)
                   .append("</").append(col.getName()).append(">\n");
            }
            xml.append("  </").append(recordElement).append(">\n");
        }
        
        xml.append("</").append(rootElement).append(">\n");
        return xml.toString().getBytes(format.getCharset());
    }

    /**
     * Writes Fixed-Width format with selected columns.
     */
    private static <T> byte[] writeFixedWidthWithColumns(
            List<T> records,
            FileFormat format,
            List<Schema.ColumnSchema> filteredColumns,
            Class<?> recordType) throws Exception {
        StringBuilder sb = new StringBuilder();
        Map<String, Field> fieldCache = buildFieldCache(recordType, filteredColumns);
        Map<String, FileColumn.Alignment> alignmentCache = buildAlignmentCache(recordType, filteredColumns);
        
        for (T record : records) {
            for (Schema.ColumnSchema col : filteredColumns) {
                Field field = fieldCache.get(col.getFieldName());
                Object value = field.get(record);
                String strValue = formatValue(value, col, format);
                
                if (col.getLength() > 0) {
                    FileColumn.Alignment alignment = alignmentCache.getOrDefault(col.getFieldName(), FileColumn.Alignment.LEFT);
                    strValue = padOrTruncate(strValue, col.getLength(), alignment);
                }
                sb.append(strValue);
            }
            sb.append(format.getLineSeparator());
        }
        
        return sb.toString().getBytes(format.getCharset());
    }

    /**
     * Builds a cache of field objects for efficient access.
     */
    private static Map<String, Field> buildFieldCache(
            Class<?> recordType,
            List<Schema.ColumnSchema> columns) {
        Map<String, Field> cache = new HashMap<>();
        for (Schema.ColumnSchema col : columns) {
            try {
                Field field = recordType.getDeclaredField(col.getFieldName());
                field.setAccessible(true);
                cache.put(col.getFieldName(), field);
            } catch (NoSuchFieldException e) {
                log.warn("Field {} not found in type {}", col.getFieldName(), recordType.getName());
            }
        }
        return cache;
    }

    /**
     * Builds a cache of alignment settings from FileColumn annotations.
     */
    private static Map<String, FileColumn.Alignment> buildAlignmentCache(
            Class<?> recordType,
            List<Schema.ColumnSchema> columns) {
        Map<String, FileColumn.Alignment> cache = new HashMap<>();
        for (Schema.ColumnSchema col : columns) {
            try {
                Field field = recordType.getDeclaredField(col.getFieldName());
                FileColumn annotation = field.getAnnotation(FileColumn.class);
                if (annotation != null) {
                    cache.put(col.getFieldName(), annotation.alignment());
                }
            } catch (NoSuchFieldException e) {
                // Field already validated in buildFieldCache
            }
        }
        return cache;
    }

    /**
     * Formats a value to string based on column schema and format.
     */
    private static String formatValue(Object value, Schema.ColumnSchema col, FileFormat format) {
        if (value == null) {
            return format.getNullValue();
        }
        
        // Apply format if specified
        if (!col.getFormat().isEmpty()) {
            if (value instanceof LocalDate) {
                return ((LocalDate) value).format(DateTimeFormatter.ofPattern(col.getFormat()));
            } else if (value instanceof LocalDateTime) {
                return ((LocalDateTime) value).format(DateTimeFormatter.ofPattern(col.getFormat()));
            } else if (value instanceof Number) {
                // Number formatting would require DecimalFormat, for now use toString
                // This is a simplification - full implementation would use proper formatting
                return value.toString();
            }
        }
        
        return value.toString();
    }

    /**
     * Adds a value to a JSON ObjectNode, handling different types.
     */
    private static void addValueToJsonNode(ObjectNode node, String fieldName, Object value, Schema.ColumnSchema col) {
        if (value == null) {
            node.putNull(fieldName);
        } else if (value instanceof String) {
            node.put(fieldName, (String) value);
        } else if (value instanceof Number) {
            if (value instanceof Integer) {
                node.put(fieldName, (Integer) value);
            } else if (value instanceof Long) {
                node.put(fieldName, (Long) value);
            } else if (value instanceof Double) {
                node.put(fieldName, (Double) value);
            } else if (value instanceof java.math.BigDecimal) {
                node.put(fieldName, (java.math.BigDecimal) value);
            } else {
                node.put(fieldName, value.toString());
            }
        } else if (value instanceof Boolean) {
            node.put(fieldName, (Boolean) value);
        } else if (value instanceof LocalDate) {
            String formatted = col.getFormat().isEmpty() 
                ? value.toString()
                : ((LocalDate) value).format(DateTimeFormatter.ofPattern(col.getFormat()));
            node.put(fieldName, formatted);
        } else if (value instanceof LocalDateTime) {
            String formatted = col.getFormat().isEmpty()
                ? value.toString()
                : ((LocalDateTime) value).format(DateTimeFormatter.ofPattern(col.getFormat()));
            node.put(fieldName, formatted);
        } else {
            node.put(fieldName, value.toString());
        }
    }

    /**
     * Escapes XML special characters.
     */
    private static String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }

    /**
     * Pads or truncates a string to the specified length.
     */
    private static String padOrTruncate(String value, int length, FileColumn.Alignment alignment) {
        if (value == null) {
            value = "";
        }
        
        if (value.length() > length) {
            return value.substring(0, length);
        }
        
        char padChar = ' ';
        
        switch (alignment) {
            case RIGHT:
                return String.format("%" + length + "s", value).replace(' ', padChar);
            case CENTER:
                int padding = length - value.length();
                int leftPad = padding / 2;
                int rightPad = padding - leftPad;
                return String.format("%" + (value.length() + leftPad) + "s", value)
                        .replace(' ', padChar) + String.format("%" + rightPad + "s", "");
            case LEFT:
            default:
                return String.format("%-" + length + "s", value).replace(' ', padChar);
        }
    }
}

