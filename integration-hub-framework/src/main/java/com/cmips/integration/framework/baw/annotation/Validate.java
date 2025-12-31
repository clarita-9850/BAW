package com.cmips.integration.framework.baw.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines validation constraints for a field or entire record.
 *
 * <p>Example usage:
 * <pre>
 * // Field-level validation
 * &#64;FileColumn(order = 1)
 * &#64;Validate(notNull = true, message = "Payment ID is required")
 * private String paymentId;
 *
 * &#64;FileColumn(order = 2)
 * &#64;Validate(min = 0, max = 1000000, message = "Amount must be between 0 and 1,000,000")
 * private BigDecimal amount;
 *
 * // Multiple validations
 * &#64;FileColumn(order = 3)
 * &#64;Validate(pattern = "^[A-Z]{2}[0-9]{6}$", message = "Invalid account format")
 * &#64;Validate(notNull = true)
 * private String accountNumber;
 *
 * // Allowed values
 * &#64;FileColumn(order = 4)
 * &#64;Validate(allowedValues = {"ACTIVE", "INACTIVE", "PENDING"})
 * private String status;
 * </pre>
 *
 * <p>Validation attributes:
 * <ul>
 *   <li>notNull - value must not be null</li>
 *   <li>notBlank - string must not be null or empty/blank</li>
 *   <li>notEmpty - collection/string must not be empty</li>
 *   <li>min/max - numeric value must be within range</li>
 *   <li>minLength/maxLength - string length constraints</li>
 *   <li>pattern - string must match regex pattern</li>
 *   <li>allowedValues - value must be one of the specified values</li>
 *   <li>validator - custom validator class for complex validations</li>
 * </ul>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(Validate.List.class)
public @interface Validate {

    /**
     * The validation rule name (legacy, for backward compatibility).
     *
     * @return the rule name
     */
    String rule() default "";

    /**
     * Error message when validation fails.
     *
     * @return the error message
     */
    String message() default "";

    /**
     * Field must not be null.
     *
     * @return true if field is required
     */
    boolean notNull() default false;

    /**
     * String field must not be blank (null, empty, or whitespace only).
     *
     * @return true if field must not be blank
     */
    boolean notBlank() default false;

    /**
     * Collection/string must not be empty.
     *
     * @return true if field must not be empty
     */
    boolean notEmpty() default false;

    /**
     * Minimum value for numeric/range validations.
     *
     * @return the minimum value
     */
    double min() default Double.MIN_VALUE;

    /**
     * Maximum value for numeric/range validations.
     *
     * @return the maximum value
     */
    double max() default Double.MAX_VALUE;

    /**
     * Minimum string length.
     *
     * @return the minimum length
     */
    int minLength() default 0;

    /**
     * Maximum string length.
     *
     * @return the maximum length
     */
    int maxLength() default Integer.MAX_VALUE;

    /**
     * Regex pattern for pattern validation.
     *
     * @return the regex pattern
     */
    String pattern() default "";

    /**
     * Allowed values for the field.
     * Value must match one of these exactly.
     *
     * @return the allowed values
     */
    String[] allowedValues() default {};

    /**
     * Custom validator class for complex validations.
     *
     * @return the validator class
     */
    Class<? extends RecordValidator<?>> validator() default NoOpValidator.class;

    /**
     * Container for repeatable @Validate annotations.
     */
    @Target({ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        Validate[] value();
    }

    /**
     * No-op validator.
     */
    final class NoOpValidator implements RecordValidator<Object> {
        @Override
        public ValidationError validate(Object record) {
            return null;
        }
    }
}
