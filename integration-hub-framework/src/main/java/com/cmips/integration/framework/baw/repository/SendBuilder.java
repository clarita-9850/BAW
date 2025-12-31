package com.cmips.integration.framework.baw.repository;

import com.cmips.integration.framework.baw.format.FileFormat;
import com.cmips.integration.framework.baw.send.RetryConfig;
import com.cmips.integration.framework.baw.send.SendResult;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Fluent builder for send operations.
 *
 * <p>Example usage:
 * <pre>
 * SendResult result = repo.send(records)
 *     .as(FileFormat.json().prettyPrint(true).build())
 *     .to(BpmSftpDestination.class)
 *     .withFilename("payments_" + dateStr + ".json")
 *     .withMetadata("batchId", batchId)
 *     .withMetadata("count", records.size())
 *     .onSuccess(r -&gt; log.info("Sent {} records", r.getRecordCount()))
 *     .onFailure(r -&gt; alert.send("Send failed: " + r.getErrorMessage()))
 *     .withRetry(RetryConfig.builder().maxAttempts(3).backoffMs(1000).build())
 *     .execute();
 *
 * // Async execution
 * CompletableFuture&lt;SendResult&gt; future = repo.send(records)
 *     .as(FileFormat.xml())
 *     .to(FinApiDestination.class)
 *     .executeAsync();
 * </pre>
 *
 * @param <T> the record type
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface SendBuilder<T> {

    /**
     * Sets the output format.
     *
     * @param format the file format
     * @return this builder
     */
    SendBuilder<T> as(FileFormat format);

    /**
     * Sets the destination by class.
     *
     * @param destinationType the destination interface class
     * @return this builder
     */
    SendBuilder<T> to(Class<?> destinationType);

    /**
     * Sets the destination by name.
     *
     * @param destinationName the destination name
     * @return this builder
     */
    SendBuilder<T> to(String destinationName);

    /**
     * Sets the filename (static).
     *
     * @param filename the filename
     * @return this builder
     */
    SendBuilder<T> withFilename(String filename);

    /**
     * Sets the filename (dynamic).
     *
     * @param filenameSupplier supplies the filename
     * @return this builder
     */
    SendBuilder<T> withFilename(Supplier<String> filenameSupplier);

    /**
     * Adds metadata to the transmission.
     *
     * @param key the metadata key
     * @param value the metadata value
     * @return this builder
     */
    SendBuilder<T> withMetadata(String key, Object value);

    /**
     * Sets success callback.
     *
     * @param callback the success handler
     * @return this builder
     */
    SendBuilder<T> onSuccess(Consumer<SendResult> callback);

    /**
     * Sets failure callback.
     *
     * @param callback the failure handler
     * @return this builder
     */
    SendBuilder<T> onFailure(Consumer<SendResult> callback);

    /**
     * Sets retry configuration.
     *
     * @param config the retry configuration
     * @return this builder
     */
    SendBuilder<T> withRetry(RetryConfig config);

    /**
     * Executes the send operation synchronously.
     *
     * @return the send result
     */
    SendResult execute();

    /**
     * Executes the send operation asynchronously.
     *
     * @return a future with the send result
     */
    CompletableFuture<SendResult> executeAsync();
}
