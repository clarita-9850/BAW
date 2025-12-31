package com.cmips.integration.framework.exception;

/**
 * Exception thrown when a data transformation fails.
 *
 * <p>This exception is thrown by transformers when they fail to convert
 * data from one format to another. Common causes include:
 * <ul>
 *   <li>Invalid input data format</li>
 *   <li>Missing required fields</li>
 *   <li>Type conversion errors</li>
 *   <li>XSLT transformation failures</li>
 *   <li>JSON/XML parsing errors</li>
 *   <li>Template rendering failures</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * public ProcessedPayment transform(RawPayment input) throws TransformationException {
 *     try {
 *         return ProcessedPayment.builder()
 *             .id(input.getPaymentId())
 *             .amount(new BigDecimal(input.getRawAmount()))
 *             .date(LocalDate.parse(input.getDateString()))
 *             .build();
 *     } catch (NumberFormatException e) {
 *         throw new TransformationException(
 *             "Invalid amount format: " + input.getRawAmount(), e, input);
 *     } catch (DateTimeParseException e) {
 *         throw new TransformationException(
 *             "Invalid date format: " + input.getDateString(), e, input);
 *     }
 * }
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class TransformationException extends IntegrationException {

    private static final long serialVersionUID = 1L;

    /**
     * The input data that failed to transform.
     */
    private final transient Object input;

    /**
     * The field that caused the transformation failure, if applicable.
     */
    private final String field;

    /**
     * The expected type or format.
     */
    private final String expectedFormat;

    /**
     * The actual value that caused the failure.
     */
    private final String actualValue;

    /**
     * Creates a new TransformationException with the specified message.
     *
     * @param message the detail message
     */
    public TransformationException(String message) {
        super(message);
        this.input = null;
        this.field = null;
        this.expectedFormat = null;
        this.actualValue = null;
    }

    /**
     * Creates a new TransformationException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public TransformationException(String message, Throwable cause) {
        super(message, cause);
        this.input = null;
        this.field = null;
        this.expectedFormat = null;
        this.actualValue = null;
    }

    /**
     * Creates a new TransformationException with input context.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     * @param input the input data that failed
     */
    public TransformationException(String message, Throwable cause, Object input) {
        super(message, cause);
        this.input = input;
        this.field = null;
        this.expectedFormat = null;
        this.actualValue = null;
    }

    /**
     * Creates a new TransformationException with field-level details.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     * @param field the field that failed
     * @param expectedFormat the expected format
     * @param actualValue the actual value
     */
    public TransformationException(String message, Throwable cause, String field,
                                     String expectedFormat, String actualValue) {
        super(message, cause);
        this.input = null;
        this.field = field;
        this.expectedFormat = expectedFormat;
        this.actualValue = actualValue;
    }

    /**
     * Returns the input data that failed to transform.
     *
     * @return the input, or {@code null} if not set
     */
    public Object getInput() {
        return input;
    }

    /**
     * Returns the field that caused the failure.
     *
     * @return the field name, or {@code null} if not applicable
     */
    public String getField() {
        return field;
    }

    /**
     * Returns the expected format.
     *
     * @return the expected format, or {@code null} if not set
     */
    public String getExpectedFormat() {
        return expectedFormat;
    }

    /**
     * Returns the actual value that caused the failure.
     *
     * @return the actual value, or {@code null} if not set
     */
    public String getActualValue() {
        return actualValue;
    }

    @Override
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder(super.getDetailedMessage());
        if (field != null) {
            sb.append(" [Field: ").append(field).append("]");
        }
        if (expectedFormat != null) {
            sb.append(" [Expected: ").append(expectedFormat).append("]");
        }
        if (actualValue != null) {
            sb.append(" [Actual: ").append(actualValue).append("]");
        }
        return sb.toString();
    }
}
