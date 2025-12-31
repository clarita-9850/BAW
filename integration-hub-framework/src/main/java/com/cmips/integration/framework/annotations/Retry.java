package com.cmips.integration.framework.annotations;

import com.cmips.integration.framework.support.BackoffStrategy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to configure retry behavior for integration components.
 *
 * <p>This annotation can be applied to classes or methods to enable
 * automatic retry on failure. It integrates with Spring Retry for
 * the actual retry logic.
 *
 * <p>Example usage on a class:
 * <pre>
 * &#64;OutputDestination(name = "sftpUploader")
 * &#64;Retry(
 *     maxAttempts = 3,
 *     backoff = BackoffStrategy.EXPONENTIAL,
 *     initialInterval = 1000,
 *     multiplier = 2.0,
 *     maxInterval = 30000,
 *     retryOn = {ConnectionException.class, SendException.class}
 * )
 * public class SftpUploader implements IOutputDestination&lt;ProcessedFile&gt; {
 *     // Implementation...
 * }
 * </pre>
 *
 * <p>Example usage on a method:
 * <pre>
 * public class PaymentService {
 *
 *     &#64;Retry(maxAttempts = 5, initialInterval = 500)
 *     public SendResult sendPayment(Payment payment) {
 *         // Implementation...
 *     }
 * }
 * </pre>
 *
 * <p>Backoff strategies:
 * <ul>
 *   <li>{@code FIXED} - Wait a fixed interval between retries</li>
 *   <li>{@code EXPONENTIAL} - Double the interval after each retry</li>
 *   <li>{@code RANDOM} - Wait a random interval within bounds</li>
 * </ul>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retry {

    /**
     * The maximum number of retry attempts.
     *
     * <p>This includes the initial attempt. For example, a value of 3 means
     * the operation will be tried up to 3 times total.
     *
     * @return the maximum attempts
     */
    int maxAttempts() default 3;

    /**
     * The backoff strategy to use between retries.
     *
     * @return the backoff strategy
     */
    BackoffStrategy backoff() default BackoffStrategy.EXPONENTIAL;

    /**
     * The initial delay in milliseconds before the first retry.
     *
     * @return the initial interval in milliseconds
     */
    long initialInterval() default 1000;

    /**
     * The multiplier for exponential backoff.
     *
     * <p>For example, with an initial interval of 1000ms and multiplier of 2.0,
     * retries will wait 1000ms, 2000ms, 4000ms, etc.
     *
     * @return the multiplier
     */
    double multiplier() default 2.0;

    /**
     * The maximum interval in milliseconds between retries.
     *
     * <p>This caps the delay for exponential backoff to prevent
     * excessively long waits.
     *
     * @return the maximum interval in milliseconds
     */
    long maxInterval() default 30000;

    /**
     * Exception types that should trigger a retry.
     *
     * <p>If empty, all exceptions trigger retries.
     *
     * @return array of exception classes to retry on
     */
    Class<? extends Throwable>[] retryOn() default {};

    /**
     * Exception types that should not trigger a retry.
     *
     * <p>These exceptions bypass retry logic even if they match {@code retryOn}.
     *
     * @return array of exception classes to not retry on
     */
    Class<? extends Throwable>[] noRetryOn() default {};

    /**
     * Whether to include the cause in retry decisions.
     *
     * <p>If {@code true}, the cause of an exception is also checked
     * against {@code retryOn} and {@code noRetryOn}.
     *
     * @return {@code true} to traverse exception causes
     */
    boolean includeCause() default true;

    /**
     * A SpEL expression that determines whether to retry.
     *
     * <p>If specified, this expression is evaluated for each failure.
     * The expression has access to the exception as {@code #exception}
     * and the attempt number as {@code #attempt}.
     *
     * @return the condition expression
     */
    String condition() default "";

    /**
     * The name of a recovery method to call after retries are exhausted.
     *
     * <p>The recovery method should have a compatible signature.
     *
     * @return the recovery method name
     */
    String recover() default "";
}
