package com.cmips.model;

public class FieldMaskingRule {
    private String fieldName;
    private MaskingType maskingType;
    private AccessLevel accessLevel;
    private String maskingPattern;
    private String reportType;
    private String description;
    private boolean enabled = true;

    public enum MaskingType {
        NONE,           // No masking
        HIDDEN,         // Hide field completely
        PARTIAL_MASK,   // Show partial data (e.g., XXX-XX-1234)
        HASH_MASK,      // Show hash value
        ANONYMIZE,      // Replace with generic value
        AGGREGATE       // Show aggregated data only
    }

    public enum AccessLevel {
        FULL_ACCESS,     // Show complete data
        MASKED_ACCESS,  // Show masked data
        HIDDEN_ACCESS   // Hide field completely
    }

    public FieldMaskingRule() {}

    public FieldMaskingRule(String fieldName, MaskingType maskingType, AccessLevel accessLevel,
                            String maskingPattern, String reportType, String description, boolean enabled) {
        this.fieldName = fieldName;
        this.maskingType = maskingType;
        this.accessLevel = accessLevel;
        this.maskingPattern = maskingPattern;
        this.reportType = reportType;
        this.description = description;
        this.enabled = enabled;
    }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public MaskingType getMaskingType() { return maskingType; }
    public void setMaskingType(MaskingType maskingType) { this.maskingType = maskingType; }

    public AccessLevel getAccessLevel() { return accessLevel; }
    public void setAccessLevel(AccessLevel accessLevel) { this.accessLevel = accessLevel; }

    public String getMaskingPattern() { return maskingPattern; }
    public void setMaskingPattern(String maskingPattern) { this.maskingPattern = maskingPattern; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}

