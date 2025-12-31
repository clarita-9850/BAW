package com.cmips.integration.framework.baw.annotation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a validation error.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {

    /**
     * The error message.
     */
    private String message;

    /**
     * The field name that failed validation (null for record-level errors).
     */
    private String fieldName;

    /**
     * The invalid value.
     */
    private Object invalidValue;

    /**
     * The line number in the source file (for parse-time validation).
     */
    private Integer lineNumber;

    /**
     * The validation rule that failed.
     */
    private String ruleName;

    /**
     * Creates a simple validation error with just a message.
     */
    public ValidationError(String message) {
        this.message = message;
    }

    /**
     * Creates a field validation error.
     */
    public ValidationError(String fieldName, String message, Object invalidValue) {
        this.fieldName = fieldName;
        this.message = message;
        this.invalidValue = invalidValue;
    }
}
