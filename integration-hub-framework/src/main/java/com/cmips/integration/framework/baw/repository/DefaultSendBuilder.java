package com.cmips.integration.framework.baw.repository;

import com.cmips.integration.framework.baw.destination.*;
import com.cmips.integration.framework.baw.exception.DestinationException;
import com.cmips.integration.framework.baw.format.FileFormat;
import com.cmips.integration.framework.baw.send.RetryConfig;
import com.cmips.integration.framework.baw.send.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Default implementation of SendBuilder.
 *
 * @param <T> the record type
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
class DefaultSendBuilder<T> implements SendBuilder<T> {

    private static final Logger log = LoggerFactory.getLogger(DefaultSendBuilder.class);

    private final List<T> records;
    private final DefaultFileRepository<T> repository;
    private FileFormat format;
    private Class<?> destinationType;
    private String destinationName;
    private Supplier<String> filenameSupplier;
    private final Map<String, Object> metadata = new HashMap<>();
    private Consumer<SendResult> onSuccess;
    private Consumer<SendResult> onFailure;
    private RetryConfig retryConfig = RetryConfig.defaults();

    DefaultSendBuilder(List<T> records, DefaultFileRepository<T> repository) {
        this.records = records;
        this.repository = repository;
    }

    @Override
    public SendBuilder<T> as(FileFormat format) {
        this.format = format;
        return this;
    }

    @Override
    public SendBuilder<T> to(Class<?> destinationType) {
        this.destinationType = destinationType;
        Destination dest = destinationType.getAnnotation(Destination.class);
        if (dest != null) {
            this.destinationName = dest.name();
        }
        return this;
    }

    @Override
    public SendBuilder<T> to(String destinationName) {
        this.destinationName = destinationName;
        return this;
    }

    @Override
    public SendBuilder<T> withFilename(String filename) {
        this.filenameSupplier = () -> filename;
        return this;
    }

    @Override
    public SendBuilder<T> withFilename(Supplier<String> filenameSupplier) {
        this.filenameSupplier = filenameSupplier;
        return this;
    }

    @Override
    public SendBuilder<T> withMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    @Override
    public SendBuilder<T> onSuccess(Consumer<SendResult> callback) {
        this.onSuccess = callback;
        return this;
    }

    @Override
    public SendBuilder<T> onFailure(Consumer<SendResult> callback) {
        this.onFailure = callback;
        return this;
    }

    @Override
    public SendBuilder<T> withRetry(RetryConfig config) {
        this.retryConfig = config;
        return this;
    }

    @Override
    public SendResult execute() {
        validate();

        SendResult result = null;
        Exception lastException = null;

        for (int attempt = 1; attempt <= retryConfig.getMaxAttempts(); attempt++) {
            try {
                result = doSend(attempt);
                if (result.isSuccess()) {
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                    return result;
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("Send attempt {} failed: {}", attempt, e.getMessage());

                if (attempt < retryConfig.getMaxAttempts()) {
                    long backoff = calculateBackoff(attempt);
                    log.debug("Waiting {}ms before retry", backoff);
                    try {
                        Thread.sleep(backoff);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // All retries exhausted
        result = SendResult.failure(
                destinationName,
                lastException != null ? lastException.getMessage() : "Send failed after retries",
                lastException
        );

        if (onFailure != null) {
            onFailure.accept(result);
        }

        return result;
    }

    @Override
    public CompletableFuture<SendResult> executeAsync() {
        return CompletableFuture.supplyAsync(this::execute);
    }

    private void validate() {
        if (format == null) {
            throw new DestinationException("Format not specified. Use as() to set format.");
        }
        if (destinationType == null && destinationName == null) {
            throw new DestinationException("Destination not specified. Use to() to set destination.");
        }
        if (filenameSupplier == null) {
            throw new DestinationException("Filename not specified. Use withFilename() to set filename.");
        }
    }

    private SendResult doSend(int attempt) throws Exception {
        String filename = filenameSupplier.get();
        byte[] content = repository.writeToBytes(records, format);

        log.info("Sending {} records ({} bytes) to {} as {}",
                records.size(), content.length, destinationName, filename);

        // Determine destination type and send
        if (destinationType != null) {
            if (destinationType.isAnnotationPresent(Sftp.class)) {
                return sendViaSftp(filename, content);
            } else if (destinationType.isAnnotationPresent(HttpApi.class)) {
                return sendViaHttp(filename, content);
            }
        }

        // For now, return a simulated success (actual implementation would use real protocols)
        return SendResult.builder()
                .success(true)
                .destinationName(destinationName)
                .filename(filename)
                .recordCount(records.size())
                .byteCount(content.length)
                .retryAttempts(attempt - 1)
                .metadata(new HashMap<>(metadata))
                .build();
    }

    private SendResult sendViaSftp(String filename, byte[] content) {
        Sftp sftp = destinationType.getAnnotation(Sftp.class);
        // This would use SftpClient to actually send
        // For now, simulate success
        log.info("SFTP: Would send to {}:{}{}/{}",
                sftp.host(), sftp.port(), sftp.remotePath(), filename);

        return SendResult.builder()
                .success(true)
                .destinationName(destinationName)
                .destinationHost(sftp.host())
                .remotePath(sftp.remotePath() + "/" + filename)
                .filename(filename)
                .recordCount(records.size())
                .byteCount(content.length)
                .metadata(new HashMap<>(metadata))
                .build();
    }

    private SendResult sendViaHttp(String filename, byte[] content) {
        HttpApi http = destinationType.getAnnotation(HttpApi.class);
        // This would use HttpClient to actually send
        // For now, simulate success
        log.info("HTTP: Would {} to {}", http.method(), http.url());

        return SendResult.builder()
                .success(true)
                .destinationName(destinationName)
                .destinationHost(http.url())
                .filename(filename)
                .recordCount(records.size())
                .byteCount(content.length)
                .metadata(new HashMap<>(metadata))
                .build();
    }

    private long calculateBackoff(int attempt) {
        long backoff = (long) (retryConfig.getBackoffMs() *
                Math.pow(retryConfig.getBackoffMultiplier(), attempt - 1));
        return Math.min(backoff, retryConfig.getMaxBackoffMs());
    }
}
