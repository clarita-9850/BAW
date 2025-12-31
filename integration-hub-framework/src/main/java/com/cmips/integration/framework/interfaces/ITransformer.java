package com.cmips.integration.framework.interfaces;

import com.cmips.integration.framework.exception.TransformationException;
import com.cmips.integration.framework.model.TransformerMetadata;
import com.cmips.integration.framework.model.ValidationResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Interface for data transformation operations.
 *
 * <p>This interface defines the contract for transformers in the integration framework.
 * Transformers convert data from one format or structure to another, enabling
 * interoperability between different systems and data formats.
 *
 * <p>Transformers can perform various operations:
 * <ul>
 *   <li>Format conversion (XML to JSON, CSV to objects)</li>
 *   <li>Field mapping and renaming</li>
 *   <li>Data enrichment and augmentation</li>
 *   <li>Filtering and validation</li>
 *   <li>Aggregation and grouping</li>
 * </ul>
 *
 * <p>Example implementation:
 * <pre>
 * &#64;Transformer(name = "paymentTransformer",
 *              description = "Transforms raw payments to processed format",
 *              inputType = RawPayment.class,
 *              outputType = ProcessedPayment.class)
 * public class PaymentTransformer implements ITransformer&lt;RawPayment, ProcessedPayment&gt; {
 *
 *     &#64;Override
 *     public ProcessedPayment transform(RawPayment input) throws TransformationException {
 *         try {
 *             return ProcessedPayment.builder()
 *                 .id(input.getPaymentId())
 *                 .amount(parseAmount(input.getRawAmount()))
 *                 .date(parseDate(input.getPaymentDate()))
 *                 .status(mapStatus(input.getStatusCode()))
 *                 .build();
 *         } catch (Exception e) {
 *             throw new TransformationException("Failed to transform payment", e);
 *         }
 *     }
 *
 *     &#64;Override
 *     public ValidationResult validate(RawPayment input) {
 *         List&lt;String&gt; errors = new ArrayList&lt;&gt;();
 *         if (input.getPaymentId() == null) {
 *             errors.add("Payment ID is required");
 *         }
 *         if (input.getRawAmount() == null || input.getRawAmount().isEmpty()) {
 *             errors.add("Amount is required");
 *         }
 *         return ValidationResult.of(errors.isEmpty(), errors);
 *     }
 * }
 * </pre>
 *
 * @param <I> the input type
 * @param <O> the output type
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ITransformer<I, O> {

    /**
     * Transforms a single input object to the output type.
     *
     * <p>This is the primary transformation method. Implementations should:
     * <ul>
     *   <li>Handle null inputs appropriately</li>
     *   <li>Throw TransformationException for conversion failures</li>
     *   <li>Be thread-safe if marked for concurrent use</li>
     * </ul>
     *
     * @param input the input object to transform
     * @return the transformed output object
     * @throws TransformationException if transformation fails
     */
    O transform(I input) throws TransformationException;

    /**
     * Transforms a list of input objects.
     *
     * <p>The default implementation applies {@link #transform(Object)} to each
     * element in the input list.
     *
     * @param inputs the list of input objects
     * @return a list of transformed objects
     * @throws TransformationException if any transformation fails
     */
    default List<O> transformAll(List<I> inputs) throws TransformationException {
        if (inputs == null) {
            throw new TransformationException("Input list cannot be null");
        }
        try {
            return inputs.stream()
                    .map(input -> {
                        try {
                            return transform(input);
                        } catch (TransformationException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            if (e.getCause() instanceof TransformationException) {
                throw (TransformationException) e.getCause();
            }
            throw new TransformationException("Batch transformation failed", e);
        }
    }

    /**
     * Validates an input object before transformation.
     *
     * <p>This method checks whether the input is valid for transformation.
     * It can be used to fail fast before attempting transformation.
     *
     * @param input the input object to validate
     * @return a ValidationResult indicating whether the input is valid
     */
    default ValidationResult validate(I input) {
        if (input == null) {
            return ValidationResult.invalid("Input cannot be null");
        }
        return ValidationResult.valid();
    }

    /**
     * Validates a list of input objects.
     *
     * @param inputs the list of inputs to validate
     * @return a combined ValidationResult for all inputs
     */
    default ValidationResult validateAll(List<I> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return ValidationResult.invalid("Input list cannot be null or empty");
        }

        List<String> allErrors = inputs.stream()
                .map(this::validate)
                .filter(result -> !result.isValid())
                .flatMap(result -> result.getErrors().stream())
                .collect(Collectors.toList());

        return allErrors.isEmpty()
                ? ValidationResult.valid()
                : ValidationResult.invalid(allErrors);
    }

    /**
     * Returns the name of this transformer.
     *
     * @return the transformer name
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Returns metadata about this transformer.
     *
     * @return transformer metadata
     */
    default TransformerMetadata getMetadata() {
        return TransformerMetadata.builder()
                .name(this.getClass().getSimpleName())
                .description("No description available")
                .build();
    }

    /**
     * Indicates whether this transformer is stateless.
     *
     * <p>Stateless transformers can be safely shared across threads
     * and reused for multiple transformations.
     *
     * @return {@code true} if this transformer is stateless
     */
    default boolean isStateless() {
        return true;
    }

    /**
     * Initializes the transformer with any required resources.
     *
     * <p>This method is called once before the transformer is used.
     * Implementations can use it to load resources, compile patterns, etc.
     */
    default void initialize() {
        // Default implementation does nothing
    }

    /**
     * Releases any resources held by the transformer.
     *
     * <p>This method is called when the transformer is no longer needed.
     */
    default void destroy() {
        // Default implementation does nothing
    }

    /**
     * Creates a composed transformer that first applies this transformer
     * and then applies the given transformer to the result.
     *
     * @param <R> the output type of the second transformer
     * @param after the transformer to apply after this one
     * @return a composed transformer
     */
    default <R> ITransformer<I, R> andThen(ITransformer<O, R> after) {
        return new ITransformer<>() {
            @Override
            public R transform(I input) throws TransformationException {
                O intermediate = ITransformer.this.transform(input);
                return after.transform(intermediate);
            }

            @Override
            public TransformerMetadata getMetadata() {
                return TransformerMetadata.builder()
                        .name(ITransformer.this.getMetadata().getName() + " -> " + after.getMetadata().getName())
                        .description("Composed transformer")
                        .build();
            }
        };
    }
}
