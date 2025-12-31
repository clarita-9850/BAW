package com.cmips.integration.framework.baw.annotation;

/**
 * Interface for custom field value transformation during type conversion.
 *
 * <p>Implement this interface to define custom logic for transforming
 * field values when converting between different File Types.
 *
 * <p>Example usage:
 * <pre>
 * public class CentsToDollarsTransformer implements FieldTransformer&lt;Long, BigDecimal&gt; {
 *     &#64;Override
 *     public BigDecimal transform(Long cents) {
 *         if (cents == null) return null;
 *         return new BigDecimal(cents).divide(BigDecimal.valueOf(100));
 *     }
 * }
 *
 * public class DateToStringTransformer implements FieldTransformer&lt;LocalDate, String&gt; {
 *     private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
 *
 *     &#64;Override
 *     public String transform(LocalDate date) {
 *         return date != null ? date.format(FORMATTER) : null;
 *     }
 * }
 * </pre>
 *
 * @param <S> the source field type
 * @param <T> the target field type
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@FunctionalInterface
public interface FieldTransformer<S, T> {

    /**
     * Transforms a source value to target value.
     *
     * @param source the source value (may be null)
     * @return the transformed value
     */
    T transform(S source);
}
