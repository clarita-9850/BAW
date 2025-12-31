package com.cmips.integration.framework.interfaces;

import com.cmips.integration.framework.model.ValidationResult;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Interface for data validation operations.
 *
 * <p>This interface defines the contract for validators in the integration framework.
 * Validators check data against defined rules and return results indicating
 * whether the data is valid and any errors found.
 *
 * <p>Validators can be used:
 * <ul>
 *   <li>Before transformation to ensure input data quality</li>
 *   <li>After transformation to verify output correctness</li>
 *   <li>Before output to ensure data meets destination requirements</li>
 *   <li>As part of data quality monitoring</li>
 * </ul>
 *
 * <p>Example implementation:
 * <pre>
 * public class PaymentValidator implements IValidator&lt;Payment&gt; {
 *
 *     private static final List&lt;ValidationRule&lt;Payment&gt;&gt; RULES = List.of(
 *         new ValidationRule&lt;&gt;("Payment ID required",
 *             p -&gt; p.getId() != null &amp;&amp; !p.getId().isBlank()),
 *         new ValidationRule&lt;&gt;("Amount must be positive",
 *             p -&gt; p.getAmount() != null &amp;&amp; p.getAmount().compareTo(BigDecimal.ZERO) &gt; 0),
 *         new ValidationRule&lt;&gt;("Date required",
 *             p -&gt; p.getDate() != null)
 *     );
 *
 *     &#64;Override
 *     public ValidationResult validate(Payment data) {
 *         List&lt;String&gt; errors = RULES.stream()
 *             .filter(rule -&gt; !rule.test(data))
 *             .map(ValidationRule::getMessage)
 *             .collect(Collectors.toList());
 *
 *         return errors.isEmpty()
 *             ? ValidationResult.valid()
 *             : ValidationResult.invalid(errors);
 *     }
 *
 *     &#64;Override
 *     public List&lt;String&gt; getRules() {
 *         return RULES.stream()
 *             .map(ValidationRule::getMessage)
 *             .collect(Collectors.toList());
 *     }
 * }
 * </pre>
 *
 * @param <T> the type of data to validate
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface IValidator<T> {

    /**
     * Validates a single data item.
     *
     * @param data the data to validate
     * @return the validation result
     */
    ValidationResult validate(T data);

    /**
     * Returns a list of rule descriptions for this validator.
     *
     * @return the list of rule descriptions
     */
    List<String> getRules();

    /**
     * Validates a list of data items.
     *
     * @param dataList the list of data to validate
     * @return a combined validation result
     */
    default ValidationResult validateAll(List<T> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return ValidationResult.invalid("Data list cannot be null or empty");
        }

        List<String> allErrors = dataList.stream()
                .map(this::validate)
                .filter(result -> !result.isValid())
                .flatMap(result -> result.getErrors().stream())
                .collect(Collectors.toList());

        return allErrors.isEmpty()
                ? ValidationResult.valid()
                : ValidationResult.invalid(allErrors);
    }

    /**
     * Validates data and returns only the valid items.
     *
     * @param dataList the list of data to validate
     * @return a list containing only valid items
     */
    default List<T> filterValid(List<T> dataList) {
        if (dataList == null) {
            return List.of();
        }
        return dataList.stream()
                .filter(data -> validate(data).isValid())
                .collect(Collectors.toList());
    }

    /**
     * Validates data and returns only the invalid items with their errors.
     *
     * @param dataList the list of data to validate
     * @return a list containing invalid items
     */
    default List<T> filterInvalid(List<T> dataList) {
        if (dataList == null) {
            return List.of();
        }
        return dataList.stream()
                .filter(data -> !validate(data).isValid())
                .collect(Collectors.toList());
    }

    /**
     * Checks if a single data item is valid.
     *
     * @param data the data to check
     * @return {@code true} if valid
     */
    default boolean isValid(T data) {
        return validate(data).isValid();
    }

    /**
     * Combines this validator with another validator.
     *
     * <p>The combined validator runs both validators and aggregates their errors.
     *
     * @param other the other validator
     * @return a combined validator
     */
    default IValidator<T> and(IValidator<T> other) {
        return new IValidator<>() {
            @Override
            public ValidationResult validate(T data) {
                ValidationResult result1 = IValidator.this.validate(data);
                ValidationResult result2 = other.validate(data);

                if (result1.isValid() && result2.isValid()) {
                    return ValidationResult.valid();
                }

                List<String> allErrors = new java.util.ArrayList<>();
                allErrors.addAll(result1.getErrors());
                allErrors.addAll(result2.getErrors());
                return ValidationResult.invalid(allErrors);
            }

            @Override
            public List<String> getRules() {
                List<String> allRules = new java.util.ArrayList<>();
                allRules.addAll(IValidator.this.getRules());
                allRules.addAll(other.getRules());
                return allRules;
            }
        };
    }

    /**
     * Creates a simple validator from a predicate and error message.
     *
     * @param <T> the data type
     * @param predicate the validation predicate
     * @param errorMessage the error message if validation fails
     * @return a validator
     */
    static <T> IValidator<T> of(Predicate<T> predicate, String errorMessage) {
        return new IValidator<>() {
            @Override
            public ValidationResult validate(T data) {
                if (data == null) {
                    return ValidationResult.invalid("Data cannot be null");
                }
                return predicate.test(data)
                        ? ValidationResult.valid()
                        : ValidationResult.invalid(errorMessage);
            }

            @Override
            public List<String> getRules() {
                return List.of(errorMessage);
            }
        };
    }

    /**
     * Returns the name of this validator.
     *
     * @return the validator name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Returns a description of this validator.
     *
     * @return the description
     */
    default String getDescription() {
        return "Validates data against " + getRules().size() + " rules";
    }
}
