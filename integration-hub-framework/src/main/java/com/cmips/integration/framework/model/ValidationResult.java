package com.cmips.integration.framework.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the result of a validation operation.
 *
 * <p>This class encapsulates the outcome of validating data, including
 * whether validation passed and any error messages. It is immutable and thread-safe.
 *
 * <p>Example usage:
 * <pre>
 * // Successful validation
 * ValidationResult valid = ValidationResult.valid();
 *
 * // Single error
 * ValidationResult invalid = ValidationResult.invalid("Payment ID is required");
 *
 * // Multiple errors
 * ValidationResult errors = ValidationResult.invalid(List.of(
 *     "Amount must be positive",
 *     "Date cannot be in the future",
 *     "Status code is invalid"
 * ));
 *
 * // Building incrementally
 * ValidationResult result = ValidationResult.builder()
 *     .addErrorIf(payment.getId() == null, "Payment ID is required")
 *     .addErrorIf(payment.getAmount() == null, "Amount is required")
 *     .addErrorIf(payment.getAmount() != null &amp;&amp; payment.getAmount().signum() &lt; 0,
 *         "Amount must be positive")
 *     .build();
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ValidationResult {

    /**
     * Singleton instance for a valid result.
     */
    private static final ValidationResult VALID = new ValidationResult(true, Collections.emptyList(), Collections.emptyList());

    /**
     * Indicates whether the validation passed.
     */
    private final boolean valid;

    /**
     * The list of error messages.
     */
    private final List<String> errors;

    /**
     * The list of warning messages.
     */
    private final List<String> warnings;

    /**
     * Private constructor.
     */
    private ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        this.valid = valid;
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
    }

    /**
     * Returns a valid result with no errors.
     *
     * @return a valid ValidationResult
     */
    public static ValidationResult valid() {
        return VALID;
    }

    /**
     * Creates an invalid result with a single error message.
     *
     * @param error the error message
     * @return an invalid ValidationResult
     */
    public static ValidationResult invalid(String error) {
        return new ValidationResult(false, List.of(error), Collections.emptyList());
    }

    /**
     * Creates an invalid result with multiple error messages.
     *
     * @param errors the list of error messages
     * @return an invalid ValidationResult
     */
    public static ValidationResult invalid(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return VALID;
        }
        return new ValidationResult(false, errors, Collections.emptyList());
    }

    /**
     * Creates a result from a boolean condition.
     *
     * @param valid whether validation passed
     * @param errors the errors if invalid
     * @return a ValidationResult
     */
    public static ValidationResult of(boolean valid, List<String> errors) {
        if (valid) {
            return VALID;
        }
        return invalid(errors);
    }

    /**
     * Returns a builder for creating ValidationResult instances.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns whether the validation passed.
     *
     * @return {@code true} if valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Returns whether there are any errors.
     *
     * @return {@code true} if there are errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Returns whether there are any warnings.
     *
     * @return {@code true} if there are warnings
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Returns the list of error messages.
     *
     * @return an unmodifiable list of errors
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Returns the list of warning messages.
     *
     * @return an unmodifiable list of warnings
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Returns the number of errors.
     *
     * @return the error count
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * Returns the first error message, if any.
     *
     * @return the first error, or null if no errors
     */
    public String getFirstError() {
        return errors.isEmpty() ? null : errors.get(0);
    }

    /**
     * Returns all errors as a single string.
     *
     * @param delimiter the delimiter between errors
     * @return the concatenated errors
     */
    public String getErrorsAsString(String delimiter) {
        return String.join(delimiter, errors);
    }

    /**
     * Combines this result with another result.
     *
     * <p>The combined result is valid only if both results are valid.
     * Errors and warnings from both results are merged.
     *
     * @param other the other result
     * @return the combined result
     */
    public ValidationResult merge(ValidationResult other) {
        if (other == null) {
            return this;
        }
        if (this.isValid() && other.isValid()) {
            if (this.warnings.isEmpty() && other.warnings.isEmpty()) {
                return VALID;
            }
        }

        List<String> mergedErrors = new ArrayList<>(this.errors);
        mergedErrors.addAll(other.errors);

        List<String> mergedWarnings = new ArrayList<>(this.warnings);
        mergedWarnings.addAll(other.warnings);

        return new ValidationResult(mergedErrors.isEmpty(), mergedErrors, mergedWarnings);
    }

    /**
     * Throws an IllegalArgumentException if this result is invalid.
     *
     * @throws IllegalArgumentException if validation failed
     */
    public void throwIfInvalid() {
        if (!valid) {
            throw new IllegalArgumentException("Validation failed: " + getErrorsAsString("; "));
        }
    }

    @Override
    public String toString() {
        if (valid) {
            return "ValidationResult{valid=true}";
        }
        return "ValidationResult{valid=false, errors=" + errors + "}";
    }

    /**
     * Builder for creating ValidationResult instances.
     */
    public static class Builder {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        private Builder() {
        }

        /**
         * Adds an error message.
         *
         * @param error the error message
         * @return this builder
         */
        public Builder addError(String error) {
            if (error != null && !error.isBlank()) {
                errors.add(error);
            }
            return this;
        }

        /**
         * Adds an error message if a condition is true.
         *
         * @param condition the condition to check
         * @param error the error message
         * @return this builder
         */
        public Builder addErrorIf(boolean condition, String error) {
            if (condition && error != null && !error.isBlank()) {
                errors.add(error);
            }
            return this;
        }

        /**
         * Adds multiple error messages.
         *
         * @param errors the error messages
         * @return this builder
         */
        public Builder addErrors(Collection<String> errors) {
            if (errors != null) {
                this.errors.addAll(errors.stream()
                        .filter(e -> e != null && !e.isBlank())
                        .collect(Collectors.toList()));
            }
            return this;
        }

        /**
         * Adds a warning message.
         *
         * @param warning the warning message
         * @return this builder
         */
        public Builder addWarning(String warning) {
            if (warning != null && !warning.isBlank()) {
                warnings.add(warning);
            }
            return this;
        }

        /**
         * Adds a warning message if a condition is true.
         *
         * @param condition the condition to check
         * @param warning the warning message
         * @return this builder
         */
        public Builder addWarningIf(boolean condition, String warning) {
            if (condition && warning != null && !warning.isBlank()) {
                warnings.add(warning);
            }
            return this;
        }

        /**
         * Builds the ValidationResult.
         *
         * @return the built ValidationResult
         */
        public ValidationResult build() {
            if (errors.isEmpty() && warnings.isEmpty()) {
                return VALID;
            }
            return new ValidationResult(errors.isEmpty(), errors, warnings);
        }
    }
}
