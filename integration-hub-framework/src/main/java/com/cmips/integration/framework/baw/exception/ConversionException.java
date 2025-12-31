package com.cmips.integration.framework.baw.exception;

/**
 * Exception thrown when type conversion fails.
 *
 * <p>This exception is thrown when:
 * <ul>
 *   <li>Field mappings are missing for conversion</li>
 *   <li>Transformer throws an exception</li>
 *   <li>Required target field has no mapping or default value</li>
 *   <li>Type mismatch between source and target fields</li>
 * </ul>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConversionException extends BawException {

    private final Class<?> sourceType;
    private final Class<?> targetType;
    private final String fieldName;

    public ConversionException(String message) {
        super(message);
        this.sourceType = null;
        this.targetType = null;
        this.fieldName = null;
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
        this.sourceType = null;
        this.targetType = null;
        this.fieldName = null;
    }

    public ConversionException(Class<?> sourceType, Class<?> targetType, String message) {
        super("Cannot convert " + sourceType.getSimpleName() + " to " +
              targetType.getSimpleName() + ": " + message);
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.fieldName = null;
    }

    public ConversionException(Class<?> sourceType, Class<?> targetType, String fieldName, String message) {
        super("Cannot convert " + sourceType.getSimpleName() + " to " +
              targetType.getSimpleName() + " (field: " + fieldName + "): " + message);
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.fieldName = fieldName;
    }

    public ConversionException(Class<?> sourceType, Class<?> targetType, String fieldName, Throwable cause) {
        super("Cannot convert " + sourceType.getSimpleName() + " to " +
              targetType.getSimpleName() + " (field: " + fieldName + "): " + cause.getMessage(), cause);
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.fieldName = fieldName;
    }

    public Class<?> getSourceType() {
        return sourceType;
    }

    public Class<?> getTargetType() {
        return targetType;
    }

    public String getFieldName() {
        return fieldName;
    }
}
