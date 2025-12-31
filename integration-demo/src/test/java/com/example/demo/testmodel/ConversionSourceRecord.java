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
 * Source record for type conversion testing.
 * Maps to ConversionTargetRecord via @MapsFrom annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FileType(name = "conversion-source", description = "Source record for conversion tests", version = "1.0")
public class ConversionSourceRecord {

    @FileId
    @FileColumn(order = 1, name = "sourceId", nullable = false)
    private Long sourceId;

    @FileColumn(order = 2, name = "firstName")
    private String firstName;

    @FileColumn(order = 3, name = "lastName")
    private String lastName;

    @FileColumn(order = 4, name = "email")
    private String email;

    @FileColumn(order = 5, name = "birthDate", format = "yyyy-MM-dd")
    private LocalDate birthDate;

    @FileColumn(order = 6, name = "salary", format = "#,##0.00")
    private BigDecimal salary;

    @FileColumn(order = 7, name = "department")
    private String department;

    @FileColumn(order = 8, name = "isActive")
    private Boolean isActive;
}
