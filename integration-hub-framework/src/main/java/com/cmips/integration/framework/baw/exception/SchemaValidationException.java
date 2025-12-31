package com.cmips.integration.framework.baw.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when a File Type class has invalid annotations.
 *
 * <p>This exception is thrown at repository creation time when:
 * <ul>
 *   <li>Missing @FileType annotation</li>
 *   <li>Missing @FileColumn order values</li>
 *   <li>Duplicate order values</li>
 *   <li>Invalid length values for fixed-width</li>
 *   <li>Unsupported field types</li>
 *   <li>Invalid format patterns</li>
 * </ul>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class SchemaValidationException extends BawException {

    private final Class<?> fileType;
    private final List<String> errors;

    public SchemaValidationException(Class<?> fileType, String message) {
        super(message);
        this.fileType = fileType;
        this.errors = Collections.singletonList(message);
    }

    public SchemaValidationException(Class<?> fileType, List<String> errors) {
        super(formatMessage(fileType, errors));
        this.fileType = fileType;
        this.errors = new ArrayList<>(errors);
    }

    public Class<?> getFileType() {
        return fileType;
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    private static String formatMessage(Class<?> fileType, List<String> errors) {
        StringBuilder sb = new StringBuilder();
        sb.append("Schema validation failed for ").append(fileType.getSimpleName()).append(":");
        for (String error : errors) {
            sb.append("\n  - ").append(error);
        }
        return sb.toString();
    }
}
