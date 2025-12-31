package com.cmips.integration.framework.baw.send;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Result of a send operation.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class SendResult {

    /**
     * Whether the send was successful.
     */
    private final boolean success;

    /**
     * Number of records sent.
     */
    private final int recordCount;

    /**
     * Number of bytes sent.
     */
    private final long byteCount;

    /**
     * Destination name.
     */
    private final String destinationName;

    /**
     * Destination host/URL.
     */
    private final String destinationHost;

    /**
     * Remote file path (for SFTP).
     */
    private final String remotePath;

    /**
     * Filename that was sent.
     */
    private final String filename;

    /**
     * Timestamp when send completed.
     */
    @Builder.Default
    private final Instant timestamp = Instant.now();

    /**
     * Error message if failed.
     */
    private final String errorMessage;

    /**
     * Exception if failed.
     */
    private final Throwable exception;

    /**
     * Number of retry attempts made.
     */
    @Builder.Default
    private final int retryAttempts = 0;

    /**
     * Additional metadata.
     */
    @Builder.Default
    private final Map<String, Object> metadata = new HashMap<>();

    /**
     * Creates a successful send result.
     */
    public static SendResult success(String destinationName, String filename, int recordCount, long byteCount) {
        return SendResult.builder()
                .success(true)
                .destinationName(destinationName)
                .filename(filename)
                .recordCount(recordCount)
                .byteCount(byteCount)
                .build();
    }

    /**
     * Creates a failed send result.
     */
    public static SendResult failure(String destinationName, String errorMessage, Throwable exception) {
        return SendResult.builder()
                .success(false)
                .destinationName(destinationName)
                .errorMessage(errorMessage)
                .exception(exception)
                .build();
    }

    /**
     * Returns true if this send failed.
     */
    public boolean isFailed() {
        return !success;
    }

    /**
     * Gets metadata value.
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key) {
        return (T) metadata.get(key);
    }

    /**
     * Gets all metadata.
     */
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }
}
