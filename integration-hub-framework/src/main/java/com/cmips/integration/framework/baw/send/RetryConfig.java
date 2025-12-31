package com.cmips.integration.framework.baw.send;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration for retry behavior on send failures.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class RetryConfig {

    /**
     * Maximum number of retry attempts.
     */
    @Builder.Default
    private int maxAttempts = 3;

    /**
     * Initial backoff delay in milliseconds.
     */
    @Builder.Default
    private long backoffMs = 1000;

    /**
     * Backoff multiplier for exponential backoff.
     */
    @Builder.Default
    private double backoffMultiplier = 2.0;

    /**
     * Maximum backoff delay in milliseconds.
     */
    @Builder.Default
    private long maxBackoffMs = 30000;

    /**
     * Whether to retry on connection errors.
     */
    @Builder.Default
    private boolean retryOnConnectionError = true;

    /**
     * Whether to retry on timeout errors.
     */
    @Builder.Default
    private boolean retryOnTimeout = true;

    /**
     * Creates a default retry configuration.
     */
    public static RetryConfig defaults() {
        return RetryConfig.builder().build();
    }

    /**
     * Creates a configuration with no retries.
     */
    public static RetryConfig noRetry() {
        return RetryConfig.builder().maxAttempts(1).build();
    }
}
