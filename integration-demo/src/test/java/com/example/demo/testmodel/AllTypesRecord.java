package com.example.demo.testmodel;

import com.cmips.integration.framework.baw.annotation.FileColumn;
import com.cmips.integration.framework.baw.annotation.FileId;
import com.cmips.integration.framework.baw.annotation.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Record containing all supported data types.
 * Used for comprehensive type conversion testing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FileType(name = "all-types-record", description = "Record with all supported types", version = "1.0")
public class AllTypesRecord {

    @FileId
    @FileColumn(order = 1, name = "id", nullable = false)
    private Long id;

    @FileColumn(order = 2, name = "stringField")
    private String stringField;

    @FileColumn(order = 3, name = "intField")
    private Integer intField;

    @FileColumn(order = 4, name = "longField")
    private Long longField;

    @FileColumn(order = 5, name = "doubleField")
    private Double doubleField;

    @FileColumn(order = 6, name = "booleanField")
    private Boolean booleanField;

    @FileColumn(order = 7, name = "bigDecimalField", format = "#,##0.0000")
    private BigDecimal bigDecimalField;

    @FileColumn(order = 8, name = "localDateField", format = "yyyy-MM-dd")
    private LocalDate localDateField;

    @FileColumn(order = 9, name = "localDateTimeField", format = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime localDateTimeField;

    @FileColumn(order = 10, name = "instantField")
    private Instant instantField;

    @FileColumn(order = 11, name = "statusField")
    private Status statusField;

    /**
     * Enum for status field testing.
     */
    public enum Status {
        ACTIVE, INACTIVE, PENDING, DELETED
    }
}
