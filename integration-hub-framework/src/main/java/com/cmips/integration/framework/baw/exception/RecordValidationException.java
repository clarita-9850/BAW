package com.cmips.integration.framework.baw.exception;

import com.cmips.integration.framework.baw.annotation.ValidationError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when records fail validation constraints.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class RecordValidationException extends BawException {

    private final List<ValidationError> errors;
    private final int totalRecords;
    private final int failedRecords;

    public RecordValidationException(String message) {
        super(message);
        this.errors = Collections.emptyList();
        this.totalRecords = 0;
        this.failedRecords = 0;
    }

    public RecordValidationException(ValidationError error) {
        super(error.getMessage());
        this.errors = Collections.singletonList(error);
        this.totalRecords = 1;
        this.failedRecords = 1;
    }

    public RecordValidationException(List<ValidationError> errors, int totalRecords, int failedRecords) {
        super(formatMessage(errors, totalRecords, failedRecords));
        this.errors = new ArrayList<>(errors);
        this.totalRecords = totalRecords;
        this.failedRecords = failedRecords;
    }

    public List<ValidationError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public int getFailedRecords() {
        return failedRecords;
    }

    public int getValidRecords() {
        return totalRecords - failedRecords;
    }

    private static String formatMessage(List<ValidationError> errors, int total, int failed) {
        StringBuilder sb = new StringBuilder();
        sb.append("Validation failed: ").append(failed).append(" of ").append(total).append(" records invalid");

        int shown = Math.min(errors.size(), 5);
        for (int i = 0; i < shown; i++) {
            ValidationError e = errors.get(i);
            sb.append("\n  - ");
            if (e.getLineNumber() != null) {
                sb.append("Line ").append(e.getLineNumber()).append(": ");
            }
            if (e.getFieldName() != null) {
                sb.append("[").append(e.getFieldName()).append("] ");
            }
            sb.append(e.getMessage());
        }

        if (errors.size() > 5) {
            sb.append("\n  ... and ").append(errors.size() - 5).append(" more errors");
        }

        return sb.toString();
    }
}
