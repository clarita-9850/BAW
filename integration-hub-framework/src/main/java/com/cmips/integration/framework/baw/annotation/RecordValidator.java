package com.cmips.integration.framework.baw.annotation;

/**
 * Interface for custom record validation logic.
 *
 * <p>Implement this interface to define custom validation rules
 * that cannot be expressed using built-in validation annotations.
 *
 * <p>Example usage:
 * <pre>
 * public class PaymentValidator implements RecordValidator&lt;PaymentRecord&gt; {
 *     &#64;Override
 *     public ValidationError validate(PaymentRecord record) {
 *         // Cross-field validation
 *         if (record.getStartDate().isAfter(record.getEndDate())) {
 *             return new ValidationError("Start date must be before end date");
 *         }
 *
 *         // Business rule validation
 *         if (record.getAmount().compareTo(BigDecimal.valueOf(10000)) > 0
 *             && record.getApprovalCode() == null) {
 *             return new ValidationError("Approval required for amounts over $10,000");
 *         }
 *
 *         return null; // Valid
 *     }
 * }
 * </pre>
 *
 * @param <T> the record type to validate
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@FunctionalInterface
public interface RecordValidator<T> {

    /**
     * Validates a record.
     *
     * @param record the record to validate
     * @return validation error if invalid, null if valid
     */
    ValidationError validate(T record);
}
