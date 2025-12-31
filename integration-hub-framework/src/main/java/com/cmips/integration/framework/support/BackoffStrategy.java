package com.cmips.integration.framework.support;

/**
 * Defines strategies for calculating delay between retry attempts.
 *
 * <p>The backoff strategy determines how long to wait before each retry attempt.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public enum BackoffStrategy {

    /**
     * Fixed delay between retries.
     * Always waits the same amount of time.
     */
    FIXED,

    /**
     * Exponential backoff with multiplier.
     * Each retry waits longer than the previous one.
     * Delay = initialInterval * (multiplier ^ attemptNumber)
     */
    EXPONENTIAL,

    /**
     * Random delay within a range.
     * Helps prevent thundering herd problems.
     */
    RANDOM,

    /**
     * Linear increase in delay.
     * Delay = initialInterval + (attemptNumber * increment)
     */
    LINEAR,

    /**
     * No delay between retries.
     */
    NONE
}
