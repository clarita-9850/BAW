package com.cmips.integration.framework.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the result of a send operation to an output destination.
 *
 * <p>This class encapsulates the outcome of sending data, including success/failure
 * status, a descriptive message, and optional metadata. It is immutable and thread-safe.
 *
 * <p>Example usage:
 * <pre>
 * // Successful send
 * SendResult success = SendResult.success("File uploaded successfully");
 *
 * // Failed send
 * SendResult failure = SendResult.failure("Connection timeout after 30s");
 *
 * // Partial success
 * SendResult partial = SendResult.partial("Uploaded 8 of 10 records");
 *
 * // With metadata
 * SendResult result = SendResult.builder()
 *     .success(true)
 *     .message("Uploaded payment batch")
 *     .metadata("fileName", "payments_20231215.xml")
 *     .metadata("recordCount", 150)
 *     .metadata("checksum", "abc123")
 *     .build();
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SendResult {

    /**
     * Indicates whether the send was successful.
     */
    private final boolean success;

    /**
     * Indicates a partial success (some records sent, some failed).
     */
    private final boolean partial;

    /**
     * A descriptive message about the result.
     */
    private final String message;

    /**
     * Additional metadata about the send operation.
     */
    private final Map<String, Object> metadata;

    /**
     * The timestamp when the result was created.
     */
    private final Instant timestamp;

    /**
     * The number of records sent (if applicable).
     */
    private final long recordsSent;

    /**
     * Private constructor used by the builder.
     */
    private SendResult(boolean success, boolean partial, String message,
                       Map<String, Object> metadata, long recordsSent) {
        this.success = success;
        this.partial = partial;
        this.message = message;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
        this.timestamp = Instant.now();
        this.recordsSent = recordsSent;
    }

    /**
     * Creates a successful result with the given message.
     *
     * @param message the success message
     * @return a successful SendResult
     */
    public static SendResult success(String message) {
        return new SendResult(true, false, message, Collections.emptyMap(), 0);
    }

    /**
     * Creates a successful result with message and record count.
     *
     * @param message the success message
     * @param recordsSent the number of records sent
     * @return a successful SendResult
     */
    public static SendResult success(String message, long recordsSent) {
        return new SendResult(true, false, message, Collections.emptyMap(), recordsSent);
    }

    /**
     * Creates a failure result with the given message.
     *
     * @param message the failure message
     * @return a failed SendResult
     */
    public static SendResult failure(String message) {
        return new SendResult(false, false, message, Collections.emptyMap(), 0);
    }

    /**
     * Creates a partial success result.
     *
     * @param message the message describing partial success
     * @return a partial SendResult
     */
    public static SendResult partial(String message) {
        return new SendResult(false, true, message, Collections.emptyMap(), 0);
    }

    /**
     * Creates a partial success result with record count.
     *
     * @param message the message describing partial success
     * @param recordsSent the number of records successfully sent
     * @return a partial SendResult
     */
    public static SendResult partial(String message, long recordsSent) {
        return new SendResult(false, true, message, Collections.emptyMap(), recordsSent);
    }

    /**
     * Returns a builder for creating SendResult instances.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns whether the send was successful.
     *
     * @return {@code true} if successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns whether the send was partially successful.
     *
     * @return {@code true} if partial success
     */
    public boolean isPartial() {
        return partial;
    }

    /**
     * Returns whether the send completely failed.
     *
     * @return {@code true} if failed
     */
    public boolean isFailure() {
        return !success && !partial;
    }

    /**
     * Returns the result message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the metadata map.
     *
     * @return an unmodifiable map of metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Returns a specific metadata value.
     *
     * @param <T> the expected type
     * @param key the metadata key
     * @param type the expected type class
     * @return an Optional containing the value if present and of the correct type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getMetadata(String key, Class<T> type) {
        Object value = metadata.get(key);
        if (value != null && type.isInstance(value)) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }

    /**
     * Returns the timestamp when this result was created.
     *
     * @return the timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the number of records sent.
     *
     * @return the records sent count
     */
    public long getRecordsSent() {
        return recordsSent;
    }

    @Override
    public String toString() {
        return "SendResult{" +
                "success=" + success +
                ", partial=" + partial +
                ", message='" + message + '\'' +
                ", recordsSent=" + recordsSent +
                ", timestamp=" + timestamp +
                '}';
    }

    /**
     * Builder for creating SendResult instances.
     */
    public static class Builder {
        private boolean success = false;
        private boolean partial = false;
        private String message = "";
        private final Map<String, Object> metadata = new HashMap<>();
        private long recordsSent = 0;

        private Builder() {
        }

        /**
         * Sets the success status.
         *
         * @param success whether the send was successful
         * @return this builder
         */
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        /**
         * Sets the partial status.
         *
         * @param partial whether the send was partially successful
         * @return this builder
         */
        public Builder partial(boolean partial) {
            this.partial = partial;
            return this;
        }

        /**
         * Sets the result message.
         *
         * @param message the message
         * @return this builder
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Adds a metadata entry.
         *
         * @param key the metadata key
         * @param value the metadata value
         * @return this builder
         */
        public Builder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        /**
         * Sets all metadata from a map.
         *
         * @param metadata the metadata map
         * @return this builder
         */
        public Builder metadata(Map<String, Object> metadata) {
            if (metadata != null) {
                this.metadata.putAll(metadata);
            }
            return this;
        }

        /**
         * Sets the number of records sent.
         *
         * @param recordsSent the record count
         * @return this builder
         */
        public Builder recordsSent(long recordsSent) {
            this.recordsSent = recordsSent;
            return this;
        }

        /**
         * Builds the SendResult.
         *
         * @return the built SendResult
         */
        public SendResult build() {
            return new SendResult(success, partial, message, metadata, recordsSent);
        }
    }
}
