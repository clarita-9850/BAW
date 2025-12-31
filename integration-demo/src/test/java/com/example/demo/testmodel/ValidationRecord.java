package com.example.demo.testmodel;

import com.cmips.integration.framework.baw.annotation.FileColumn;
import com.cmips.integration.framework.baw.annotation.FileId;
import com.cmips.integration.framework.baw.annotation.FileType;
import com.cmips.integration.framework.baw.annotation.Validate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Record with validation constraints for testing validation framework.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FileType(name = "validation-record", description = "Validation test record", version = "1.0")
public class ValidationRecord {

    @FileId
    @FileColumn(order = 1, name = "id", nullable = false)
    @Validate(notNull = true, min = 1)
    private Long id;

    @FileColumn(order = 2, name = "name", nullable = false)
    @Validate(notNull = true, minLength = 2, maxLength = 100)
    private String name;

    @FileColumn(order = 3, name = "email")
    @Validate(pattern = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Invalid email format")
    private String email;

    @FileColumn(order = 4, name = "age")
    @Validate(min = 0, max = 150, message = "Age must be between 0 and 150")
    private Integer age;

    @FileColumn(order = 5, name = "salary", format = "#,##0.00")
    @Validate(min = 0, message = "Salary cannot be negative")
    private BigDecimal salary;

    @FileColumn(order = 6, name = "status")
    @Validate(allowedValues = {"ACTIVE", "INACTIVE", "PENDING"}, message = "Invalid status value")
    private String status;

    @FileColumn(order = 7, name = "code")
    @Validate(minLength = 3, maxLength = 10, pattern = "^[A-Z0-9]+$", message = "Code must be 3-10 uppercase alphanumeric characters")
    private String code;

    @FileColumn(order = 8, name = "percentage")
    @Validate(min = 0, max = 100, message = "Percentage must be between 0 and 100")
    private Double percentage;
}
