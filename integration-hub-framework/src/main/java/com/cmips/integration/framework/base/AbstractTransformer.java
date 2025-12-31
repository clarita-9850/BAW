package com.cmips.integration.framework.base;

import com.cmips.integration.framework.exception.TransformationException;
import com.cmips.integration.framework.interfaces.ITransformer;
import com.cmips.integration.framework.model.TransformerMetadata;
import com.cmips.integration.framework.model.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base implementation of ITransformer with validation support.
 *
 * <p>This class provides common transformation infrastructure including
 * validation, logging, and error handling. Subclasses only need to
 * implement the core transformation logic.
 *
 * <p>Example implementation:
 * <pre>
 * &#64;Transformer(name = "paymentTransformer",
 *              description = "Transforms raw payments to processed format",
 *              inputType = RawPayment.class,
 *              outputType = ProcessedPayment.class)
 * public class PaymentTransformer extends AbstractTransformer&lt;RawPayment, ProcessedPayment&gt; {
 *
 *     &#64;Override
 *     protected ProcessedPayment doTransform(RawPayment input) throws TransformationException {
 *         return ProcessedPayment.builder()
 *             .id(input.getPaymentId())
 *             .amount(new BigDecimal(input.getRawAmount()))
 *             .date(LocalDate.parse(input.getDateString()))
 *             .status(mapStatus(input.getStatusCode()))
 *             .build();
 *     }
 *
 *     &#64;Override
 *     protected ValidationResult doValidate(RawPayment input) {
 *         return ValidationResult.builder()
 *             .addErrorIf(input.getPaymentId() == null, "Payment ID is required")
 *             .addErrorIf(input.getRawAmount() == null, "Amount is required")
 *             .build();
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
public abstract class AbstractTransformer<I, O> implements ITransformer<I, O> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private boolean validateBeforeTransform = true;
    private boolean failOnValidationError = true;
    private long transformedCount = 0;
    private long errorCount = 0;

    @Override
    public O transform(I input) throws TransformationException {
        if (input == null) {
            throw new TransformationException("Input cannot be null");
        }

        // Validate if enabled
        if (validateBeforeTransform) {
            ValidationResult validation = validate(input);
            if (!validation.isValid()) {
                if (failOnValidationError) {
                    throw new TransformationException(
                            "Validation failed: " + validation.getErrorsAsString("; "));
                }
                log.warn("Validation warnings for input: {}", validation.getErrorsAsString("; "));
            }
        }

        try {
            O result = doTransform(input);
            transformedCount++;
            log.trace("Transformed: {} -> {}", input, result);
            return result;
        } catch (TransformationException e) {
            errorCount++;
            throw e;
        } catch (Exception e) {
            errorCount++;
            throw new TransformationException("Transformation failed", e, input);
        }
    }

    /**
     * Performs the actual transformation.
     *
     * <p>Subclasses must implement this method to provide the transformation logic.
     *
     * @param input the input object
     * @return the transformed output object
     * @throws TransformationException if transformation fails
     */
    protected abstract O doTransform(I input) throws TransformationException;

    @Override
    public ValidationResult validate(I input) {
        if (input == null) {
            return ValidationResult.invalid("Input cannot be null");
        }

        try {
            return doValidate(input);
        } catch (Exception e) {
            log.warn("Validation error for input: {}", e.getMessage());
            return ValidationResult.invalid("Validation error: " + e.getMessage());
        }
    }

    /**
     * Performs custom validation on the input.
     *
     * <p>Subclasses can override this method to provide input-specific validation.
     * The default implementation returns a valid result.
     *
     * @param input the input object to validate
     * @return the validation result
     */
    protected ValidationResult doValidate(I input) {
        return ValidationResult.valid();
    }

    @Override
    public List<O> transformAll(List<I> inputs) throws TransformationException {
        if (inputs == null) {
            throw new TransformationException("Input list cannot be null");
        }

        List<O> results = new ArrayList<>(inputs.size());
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < inputs.size(); i++) {
            try {
                O result = transform(inputs.get(i));
                results.add(result);
            } catch (TransformationException e) {
                String error = String.format("Item %d: %s", i, e.getMessage());
                if (failOnValidationError) {
                    throw new TransformationException(error, e);
                }
                errors.add(error);
                log.warn("Skipping item {}: {}", i, e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            log.warn("Transformation completed with {} errors", errors.size());
        }

        return results;
    }

    @Override
    public TransformerMetadata getMetadata() {
        return TransformerMetadata.builder()
                .name(getClass().getSimpleName())
                .description("Transformer implementation")
                .stateless(isStateless())
                .attribute("transformedCount", transformedCount)
                .attribute("errorCount", errorCount)
                .attribute("validateBeforeTransform", validateBeforeTransform)
                .build();
    }

    @Override
    public boolean isStateless() {
        return true;
    }

    /**
     * Sets whether to validate input before transformation.
     *
     * @param validateBeforeTransform {@code true} to enable validation
     */
    public void setValidateBeforeTransform(boolean validateBeforeTransform) {
        this.validateBeforeTransform = validateBeforeTransform;
    }

    /**
     * Sets whether to fail on validation errors.
     *
     * @param failOnValidationError {@code true} to throw exception on validation failure
     */
    public void setFailOnValidationError(boolean failOnValidationError) {
        this.failOnValidationError = failOnValidationError;
    }

    /**
     * Returns the number of successfully transformed items.
     *
     * @return the transformed count
     */
    public long getTransformedCount() {
        return transformedCount;
    }

    /**
     * Returns the number of transformation errors.
     *
     * @return the error count
     */
    public long getErrorCount() {
        return errorCount;
    }

    /**
     * Resets the transformation statistics.
     */
    public void resetStats() {
        transformedCount = 0;
        errorCount = 0;
    }
}
