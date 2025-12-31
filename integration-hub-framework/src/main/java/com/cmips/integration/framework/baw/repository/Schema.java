package com.cmips.integration.framework.baw.repository;

import com.cmips.integration.framework.baw.annotation.FileColumn;
import com.cmips.integration.framework.baw.annotation.FileId;
import com.cmips.integration.framework.baw.annotation.FileType;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Schema information for a file type.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class Schema {

    private final String name;
    private final String description;
    private final String version;
    private final Class<?> type;
    private final List<ColumnSchema> columns;
    private final List<ColumnSchema> idColumns;

    /**
     * Extracts schema from a file type class.
     */
    public static Schema from(Class<?> type) {
        FileType fileType = type.getAnnotation(FileType.class);
        if (fileType == null) {
            throw new IllegalArgumentException("Class " + type.getName() + " is not annotated with @FileType");
        }

        List<ColumnSchema> columns = new ArrayList<>();
        List<ColumnSchema> idColumns = new ArrayList<>();

        for (Field field : type.getDeclaredFields()) {
            FileColumn columnAnn = field.getAnnotation(FileColumn.class);
            if (columnAnn == null) {
                continue;
            }

            ColumnSchema column = ColumnSchema.builder()
                    .name(columnAnn.name().isEmpty() ? field.getName() : columnAnn.name())
                    .fieldName(field.getName())
                    .order(columnAnn.order())
                    .length(columnAnn.length())
                    .nullable(columnAnn.nullable())
                    .format(columnAnn.format())
                    .defaultValue(columnAnn.defaultValue())
                    .javaType(field.getType())
                    .isId(field.isAnnotationPresent(FileId.class))
                    .build();

            columns.add(column);

            if (column.isId()) {
                idColumns.add(column);
            }
        }

        columns.sort(Comparator.comparingInt(ColumnSchema::getOrder));

        return Schema.builder()
                .name(fileType.name())
                .description(fileType.description())
                .version(fileType.version())
                .type(type)
                .columns(Collections.unmodifiableList(columns))
                .idColumns(Collections.unmodifiableList(idColumns))
                .build();
    }

    /**
     * Returns column by name.
     */
    public ColumnSchema getColumn(String name) {
        return columns.stream()
                .filter(c -> c.getName().equals(name) || c.getFieldName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns true if this schema has identity columns.
     */
    public boolean hasIdentity() {
        return !idColumns.isEmpty();
    }

    /**
     * Column schema information.
     */
    @Data
    @Builder
    public static class ColumnSchema {
        private final String name;
        private final String fieldName;
        private final int order;
        private final int length;
        private final boolean nullable;
        private final String format;
        private final String defaultValue;
        private final Class<?> javaType;
        private final boolean isId;
    }
}
