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
 * Record with composite (multi-field) primary key.
 * Used for testing deduplication with multiple identity fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FileType(name = "composite-key-record", description = "Composite key test record", version = "1.0")
public class CompositeKeyRecord {

    @FileId(order = 1)
    @FileColumn(order = 1, name = "regionCode", nullable = false)
    private String regionCode;

    @FileId(order = 2)
    @FileColumn(order = 2, name = "accountNumber", nullable = false)
    private Long accountNumber;

    @FileId(order = 3)
    @FileColumn(order = 3, name = "transactionDate", nullable = false, format = "yyyy-MM-dd")
    private LocalDate transactionDate;

    @FileColumn(order = 4, name = "amount", format = "#,##0.00")
    private BigDecimal amount;

    @FileColumn(order = 5, name = "description")
    private String description;

    @FileColumn(order = 6, name = "category")
    private String category;
}
