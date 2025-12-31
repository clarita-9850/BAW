package com.cmips.integration.framework.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to configure circuit breaker behavior for integration components.
 *
 * <p>The circuit breaker pattern prevents cascading failures by temporarily
 * blocking calls to a failing service. When failures exceed a threshold,
 * the circuit "opens" and calls fail fast without attempting the operation.
 * After a reset timeout, the circuit enters "half-open" state and allows
 * a test request through. If it succeeds, the circuit closes; if it fails,
 * the circuit reopens.
 *
 * <p>Circuit states:
 * <ul>
 *   <li><b>CLOSED</b> - Normal operation, calls pass through</li>
 *   <li><b>OPEN</b> - Failures exceeded threshold, calls fail fast</li>
 *   <li><b>HALF_OPEN</b> - Testing if service has recovered</li>
 * </ul>
 *
 * <p>Example usage on a class:
 * <pre>
 * &#64;OutputDestination(name = "externalApiClient")
 * &#64;CircuitBreaker(
 *     failureThreshold = 5,
 *     resetTimeout = 60000,
 *     timeout = 5000
 * )
 * public class ExternalApiClient implements IOutputDestination&lt;Request&gt; {
 *     // Implementation...
 * }
 * </pre>
 *
 * <p>Example usage on a method:
 * <pre>
 * public class PaymentGateway {
 *
 *     &#64;CircuitBreaker(failureThreshold = 3, resetTimeout = 30000)
 *     public PaymentResult processPayment(Payment payment) {
 *         // Call external payment service
 *     }
 * }
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CircuitBreaker {

    /**
     * The number of consecutive failures required to open the circuit.
     *
     * <p>When this threshold is reached, the circuit opens and subsequent
     * calls fail immediately without attempting the operation.
     *
     * @return the failure threshold
     */
    int failureThreshold() default 5;

    /**
     * The time in milliseconds to wait before attempting to close the circuit.
     *
     * <p>After the circuit opens, it remains open for this duration.
     * After the timeout, it enters half-open state and allows a test request.
     *
     * @return the reset timeout in milliseconds
     */
    long resetTimeout() default 60000;

    /**
     * The maximum time in milliseconds to wait for an operation to complete.
     *
     * <p>If the operation takes longer than this, it is considered a failure
     * for circuit breaker purposes.
     *
     * @return the operation timeout in milliseconds
     */
    long timeout() default 30000;

    /**
     * The number of successful calls required to close the circuit from half-open state.
     *
     * @return the success threshold
     */
    int successThreshold() default 1;

    /**
     * Exception types that should count as failures for the circuit breaker.
     *
     * <p>If empty, all exceptions count as failures.
     *
     * @return array of exception classes that trigger the circuit
     */
    Class<? extends Throwable>[] failOn() default {};

    /**
     * Exception types that should not count as failures.
     *
     * <p>These exceptions bypass circuit breaker failure counting.
     *
     * @return array of exception classes to ignore
     */
    Class<? extends Throwable>[] ignoreOn() default {};

    /**
     * The name of the circuit breaker instance.
     *
     * <p>Multiple methods can share the same circuit breaker by using
     * the same name. If not specified, each annotated element gets
     * its own circuit breaker.
     *
     * @return the circuit breaker name
     */
    String name() default "";

    /**
     * The fallback method to call when the circuit is open.
     *
     * <p>The fallback method should have a compatible return type.
     * It receives the original parameters plus the exception as the last parameter.
     *
     * @return the fallback method name
     */
    String fallback() default "";

    /**
     * Whether to track metrics for this circuit breaker.
     *
     * @return {@code true} to enable metrics
     */
    boolean enableMetrics() default true;

    /**
     * The sliding window size for failure rate calculation.
     *
     * <p>The failure rate is calculated over this many recent calls.
     *
     * @return the window size
     */
    int slidingWindowSize() default 10;

    /**
     * The failure rate percentage threshold to open the circuit.
     *
     * <p>If the failure rate exceeds this percentage, the circuit opens.
     * Used in conjunction with sliding window calculation.
     *
     * @return the failure rate threshold (0-100)
     */
    int failureRateThreshold() default 50;
}
