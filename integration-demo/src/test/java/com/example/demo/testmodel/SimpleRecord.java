package com.example.demo.testmodel;

import com.cmips.integration.framework.baw.annotation.FileColumn;
import com.cmips.integration.framework.baw.annotation.FileId;
import com.cmips.integration.framework.baw.annotation.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Simple test record with basic fields.
 * Used for basic CRUD operations testing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FileType(name = "simple-record", description = "Simple test record", version = "1.0")
public class SimpleRecord {

    @FileId
    @FileColumn(order = 1, name = "id", nullable = false)
    private Long id;

    @FileColumn(order = 2, name = "name", nullable = false)
    private String name;

    @FileColumn(order = 3, name = "amount", format = "#,##0.00")
    private BigDecimal amount;

    @FileColumn(order = 4, name = "date", format = "yyyy-MM-dd")
    private LocalDate date;
}
