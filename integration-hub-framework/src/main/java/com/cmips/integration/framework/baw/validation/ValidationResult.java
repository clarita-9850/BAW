package com.cmips.integration.framework.baw.validation;

import com.cmips.integration.framework.baw.annotation.ValidationError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of record validation.
 *
 * @param <T> the record type
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class ValidationResult<T> {

    /**
     * Records that passed validation.
     */
    private final List<T> validRecords;

    /**
     * Records that failed validation.
     */
    private final List<T> invalidRecords;

    /**
     * Validation errors.
     */
    private final List<ValidationError> errors;

    /**
     * Total records validated.
     */
    private final int totalCount;

    private ValidationResult(List<T> validRecords, List<T> invalidRecords, List<ValidationError> errors, int totalCount) {
        this.validRecords = validRecords;
        this.invalidRecords = invalidRecords;
        this.errors = errors;
        this.totalCount = totalCount;
    }

    /**
     * Creates an empty result builder.
     */
    public static <T> Builder<T> forType(Class<T> type) {
        return new Builder<>();
    }

    /**
     * Creates a builder.
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Returns true if all records are valid.
     */
    public boolean isValid() {
        return errors == null || errors.isEmpty();
    }

    /**
     * Returns true if any records are invalid.
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Returns the valid records.
     */
    public List<T> getValidRecords() {
        return validRecords;
    }

    /**
     * Returns the invalid records.
     */
    public List<T> getInvalidRecords() {
        return invalidRecords;
    }

    /**
     * Returns the validation errors.
     */
    public List<ValidationError> getErrors() {
        return errors;
    }

    /**
     * Returns the total count.
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Returns the number of valid records.
     */
    public int getValidCount() {
        return validRecords != null ? validRecords.size() : 0;
    }

    /**
     * Returns the number of invalid records.
     */
    public int getInvalidCount() {
        return invalidRecords != null ? invalidRecords.size() : 0;
    }

    /**
     * Returns the error count.
     */
    public int getErrorCount() {
        return errors != null ? errors.size() : 0;
    }

    /**
     * Throws RecordValidationException if there are errors.
     */
    public void throwIfInvalid() {
        if (hasErrors()) {
            throw new com.cmips.integration.framework.baw.exception.RecordValidationException(
                    errors, totalCount, getInvalidCount()
            );
        }
    }

    /**
     * Builder for ValidationResult.
     */
    public static class Builder<T> {
        private final List<T> validRecords = new ArrayList<>();
        private final List<T> invalidRecords = new ArrayList<>();
        private final List<ValidationError> errors = new ArrayList<>();

        public Builder<T> addValid(T record) {
            validRecords.add(record);
            return this;
        }

        public Builder<T> addInvalid(T record, ValidationError error) {
            invalidRecords.add(record);
            errors.add(error);
            return this;
        }

        public Builder<T> addError(ValidationError error) {
            errors.add(error);
            return this;
        }

        public Builder<T> validRecords(List<T> records) {
            this.validRecords.clear();
            this.validRecords.addAll(records);
            return this;
        }

        public Builder<T> invalidRecords(List<T> records) {
            this.invalidRecords.clear();
            this.invalidRecords.addAll(records);
            return this;
        }

        public Builder<T> errors(List<ValidationError> errors) {
            this.errors.clear();
            this.errors.addAll(errors);
            return this;
        }

        public ValidationResult<T> build() {
            return new ValidationResult<>(
                    Collections.unmodifiableList(new ArrayList<>(validRecords)),
                    Collections.unmodifiableList(new ArrayList<>(invalidRecords)),
                    Collections.unmodifiableList(new ArrayList<>(errors)),
                    validRecords.size() + invalidRecords.size()
            );
        }
    }
}
