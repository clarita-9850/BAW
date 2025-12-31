package com.example.demo.testmodel;

import com.cmips.integration.framework.baw.annotation.FieldTransformer;
import com.cmips.integration.framework.baw.annotation.FileColumn;
import com.cmips.integration.framework.baw.annotation.FileId;
import com.cmips.integration.framework.baw.annotation.FileType;
import com.cmips.integration.framework.baw.annotation.MapsFrom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Target record for type conversion testing.
 * Demonstrates @MapsFrom field mapping with transformers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FileType(name = "conversion-target", description = "Target record for conversion tests", version = "1.0")
public class ConversionTargetRecord {

    @FileId
    @FileColumn(order = 1, name = "id", nullable = false)
    @MapsFrom(source = ConversionSourceRecord.class, field = "sourceId")
    private Long id;

    @FileColumn(order = 2, name = "fullName")
    @MapsFrom(source = ConversionSourceRecord.class, field = "firstName", transformer = FullNameTransformer.class)
    private String fullName;

    @FileColumn(order = 3, name = "contactEmail")
    @MapsFrom(source = ConversionSourceRecord.class, field = "email", transformer = LowerCaseTransformer.class)
    private String contactEmail;

    @FileColumn(order = 4, name = "annualSalary", format = "#,##0.00")
    @MapsFrom(source = ConversionSourceRecord.class, field = "salary", transformer = AnnualSalaryTransformer.class)
    private BigDecimal annualSalary;

    @FileColumn(order = 5, name = "status")
    @MapsFrom(source = ConversionSourceRecord.class, field = "isActive", transformer = StatusTransformer.class)
    private String status;

    @FileColumn(order = 6, name = "dept")
    @MapsFrom(source = ConversionSourceRecord.class, field = "department")
    private String dept;

    /**
     * Transformer that converts first name to uppercase.
     */
    public static class FullNameTransformer implements FieldTransformer<String, String> {
        @Override
        public String transform(String value) {
            return value != null ? value.toUpperCase() : null;
        }
    }

    /**
     * Transformer that converts string to lowercase.
     */
    public static class LowerCaseTransformer implements FieldTransformer<String, String> {
        @Override
        public String transform(String value) {
            return value != null ? value.toLowerCase() : null;
        }
    }

    /**
     * Transformer that multiplies salary by 12 for annual.
     */
    public static class AnnualSalaryTransformer implements FieldTransformer<BigDecimal, BigDecimal> {
        @Override
        public BigDecimal transform(BigDecimal value) {
            return value != null ? value.multiply(new BigDecimal("12")) : null;
        }
    }

    /**
     * Transformer that converts boolean to status string.
     */
    public static class StatusTransformer implements FieldTransformer<Boolean, String> {
        @Override
        public String transform(Boolean value) {
            if (value == null) return "UNKNOWN";
            return value ? "ACTIVE" : "INACTIVE";
        }
    }
}
