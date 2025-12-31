package com.example.demo.model;

import com.cmips.integration.framework.baw.annotation.FileColumn;
import com.cmips.integration.framework.baw.annotation.FileId;
import com.cmips.integration.framework.baw.annotation.FileType;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Employee domain model with BAW Framework annotations.
 *
 * Demonstrates:
 * - @FileType for schema definition
 * - @FileColumn for field mapping
 * - @FileId for identity/deduplication
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FileType(name = "employee-record", description = "Employee record for HR system", version = "1.0")
public class Employee {

    @FileId
    @FileColumn(order = 1, name = "id", nullable = false)
    @JacksonXmlProperty(localName = "id")
    private Long id;

    @FileColumn(order = 2, name = "name", nullable = false)
    @JacksonXmlProperty(localName = "name")
    private String name;

    @FileColumn(order = 3, name = "department")
    @JacksonXmlProperty(localName = "department")
    private String department;

    @FileColumn(order = 4, name = "salary", format = "#,##0.00")
    @JacksonXmlProperty(localName = "salary")
    private BigDecimal salary;
}
