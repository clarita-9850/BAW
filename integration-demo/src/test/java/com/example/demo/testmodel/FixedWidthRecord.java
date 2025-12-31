package com.example.demo.testmodel;

import com.cmips.integration.framework.baw.annotation.FileColumn;
import com.cmips.integration.framework.baw.annotation.FileId;
import com.cmips.integration.framework.baw.annotation.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Record for fixed-width file format testing.
 * Each field has explicit length and padding configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FileType(name = "fixed-width-record", description = "Fixed-width format test record", version = "1.0")
public class FixedWidthRecord {

    @FileId
    @FileColumn(order = 1, name = "id", length = 10, alignment = FileColumn.Alignment.RIGHT, padChar = '0')
    private Long id;

    @FileColumn(order = 2, name = "name", length = 30, alignment = FileColumn.Alignment.LEFT, padChar = ' ')
    private String name;

    @FileColumn(order = 3, name = "code", length = 5, alignment = FileColumn.Alignment.RIGHT, padChar = '0')
    private String code;

    @FileColumn(order = 4, name = "amount", length = 15, alignment = FileColumn.Alignment.RIGHT, padChar = ' ', format = "#,##0.00")
    private BigDecimal amount;

    @FileColumn(order = 5, name = "status", length = 1)
    private String status;

    @FileColumn(order = 6, name = "description", length = 50, alignment = FileColumn.Alignment.LEFT, padChar = ' ')
    private String description;
}
