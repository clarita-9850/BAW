package com.cmips.integration.framework.baw.format;

import com.cmips.integration.framework.baw.annotation.FileColumn;
import com.cmips.integration.framework.baw.annotation.FileId;
import com.cmips.integration.framework.baw.annotation.FileType;
import com.cmips.integration.framework.baw.exception.SchemaValidationException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class for format handlers with common schema processing logic.
 *
 * @param <T> the record type
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class AbstractFormatHandler<T> implements FormatParser<T>, FormatWriter<T> {

    private static final Set<Class<?>> SUPPORTED_TYPES = Set.of(
            String.class,
            Integer.class, int.class,
            Long.class, long.class,
            Short.class, short.class,
            Byte.class, byte.class,
            BigDecimal.class, BigInteger.class,
            Double.class, double.class,
            Float.class, float.class,
            Boolean.class, boolean.class,
            LocalDate.class, LocalDateTime.class, LocalTime.class,
            Instant.class, ZonedDateTime.class
    );

    protected final Class<T> recordType;
    protected final FileType fileTypeAnnotation;
    protected final List<ColumnInfo> columns;
    protected final List<ColumnInfo> idColumns;

    protected AbstractFormatHandler(Class<T> recordType) {
        this.recordType = recordType;
        this.fileTypeAnnotation = recordType.getAnnotation(FileType.class);

        if (fileTypeAnnotation == null) {
            throw new SchemaValidationException(recordType, "Missing @FileType annotation");
        }

        this.columns = new ArrayList<>();
        this.idColumns = new ArrayList<>();
        validateAndLoadSchema();
    }

    private void validateAndLoadSchema() {
        List<String> errors = new ArrayList<>();
        Set<Integer> usedOrders = new HashSet<>();

        for (Field field : recordType.getDeclaredFields()) {
            FileColumn columnAnn = field.getAnnotation(FileColumn.class);
            if (columnAnn == null) {
                continue;
            }

            // Validate field type
            Class<?> fieldType = field.getType();
            if (!SUPPORTED_TYPES.contains(fieldType) && !fieldType.isEnum()) {
                errors.add("Unsupported field type for " + field.getName() + ": " + fieldType.getName());
            }

            // Validate order
            int order = columnAnn.order();
            if (order < 1) {
                errors.add("Invalid order for " + field.getName() + ": must be >= 1");
            }
            if (!usedOrders.add(order)) {
                errors.add("Duplicate order value " + order + " for field " + field.getName());
            }

            // Validate format pattern
            String format = columnAnn.format();
            if (!format.isEmpty()) {
                try {
                    if (isDateTimeType(fieldType)) {
                        DateTimeFormatter.ofPattern(format);
                    }
                } catch (Exception e) {
                    errors.add("Invalid format pattern for " + field.getName() + ": " + format);
                }
            }

            field.setAccessible(true);
            ColumnInfo info = new ColumnInfo(field, columnAnn);
            columns.add(info);

            if (field.isAnnotationPresent(FileId.class)) {
                idColumns.add(info);
            }
        }

        // Sort columns by order
        columns.sort(Comparator.comparingInt(c -> c.annotation.order()));
        idColumns.sort(Comparator.comparingInt(c -> {
            FileId fileId = c.field.getAnnotation(FileId.class);
            return fileId != null ? fileId.order() : 0;
        }));

        if (!errors.isEmpty()) {
            throw new SchemaValidationException(recordType, errors);
        }
    }

    private boolean isDateTimeType(Class<?> type) {
        return type == LocalDate.class || type == LocalDateTime.class ||
               type == LocalTime.class || type == Instant.class ||
               type == ZonedDateTime.class;
    }

    @Override
    public Class<T> getRecordType() {
        return recordType;
    }

    protected List<ColumnInfo> getColumns() {
        return columns;
    }

    protected List<ColumnInfo> getIdColumns() {
        return idColumns;
    }

    /**
     * Column metadata holder.
     */
    protected static class ColumnInfo {
        public final Field field;
        public final FileColumn annotation;

        public ColumnInfo(Field field, FileColumn annotation) {
            this.field = field;
            this.annotation = annotation;
        }

        public String getName() {
            String name = annotation.name();
            return name.isEmpty() ? field.getName() : name;
        }
    }
}
