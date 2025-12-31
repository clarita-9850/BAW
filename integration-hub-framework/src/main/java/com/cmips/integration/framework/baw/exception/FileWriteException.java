package com.cmips.integration.framework.baw.exception;

import java.nio.file.Path;

/**
 * Exception thrown when file writing fails.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class FileWriteException extends BawException {

    private final Path filePath;
    private final Integer recordIndex;

    public FileWriteException(String message) {
        super(message);
        this.filePath = null;
        this.recordIndex = null;
    }

    public FileWriteException(String message, Throwable cause) {
        super(message, cause);
        this.filePath = null;
        this.recordIndex = null;
    }

    public FileWriteException(Path filePath, String message, Throwable cause) {
        super("Failed to write " + filePath + ": " + message, cause);
        this.filePath = filePath;
        this.recordIndex = null;
    }

    public FileWriteException(Path filePath, int recordIndex, String message, Throwable cause) {
        super("Failed to write record " + recordIndex + " to " + filePath + ": " + message, cause);
        this.filePath = filePath;
        this.recordIndex = recordIndex;
    }

    public Path getFilePath() {
        return filePath;
    }

    public Integer getRecordIndex() {
        return recordIndex;
    }
}
