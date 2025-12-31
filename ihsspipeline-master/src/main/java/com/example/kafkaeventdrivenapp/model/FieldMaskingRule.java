package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
