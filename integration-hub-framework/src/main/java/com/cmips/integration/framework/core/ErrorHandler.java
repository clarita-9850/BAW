package com.cmips.integration.framework.core;

import com.cmips.integration.framework.exception.IntegrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 * Centralized error handling for the integration framework.
 *
 * <p>The ErrorHandler provides consistent error handling, logging, and tracking
 * across all integration flows. It maintains error statistics and supports
 * custom error handlers for specific error types.
 *
 * <p>Example usage:
 * <pre>
 * &#64;Autowired
 * private ErrorHandler errorHandler;
 *
 * // Handle an exception
 * errorHandler.handle("paymentFlow", exception);
 *
 * // Register a custom handler for specific exceptions
 * errorHandler.registerHandler(ConnectionException.class, (flowName, ex) -> {
 *     alertService.sendConnectionAlert(flowName, ex);
 * });
 *
 * // Get error statistics
 * ErrorHandler.ErrorStats stats = errorHandler.getStats("paymentFlow");
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    private final Map<String, ErrorStats> flowStats = new ConcurrentHashMap<>();
    private final Map<Class<? extends Throwable>, BiConsumer<String, Throwable>> customHandlers = new ConcurrentHashMap<>();
    private final List<ErrorRecord> recentErrors = Collections.synchronizedList(new ArrayList<>());
    private final int maxRecentErrors;

    /**
     * Creates an ErrorHandler with default settings.
     */
    public ErrorHandler() {
        this(100);
    }

    /**
     * Creates an ErrorHandler with a custom recent errors limit.
     *
     * @param maxRecentErrors maximum number of recent errors to retain
     */
    public ErrorHandler(int maxRecentErrors) {
        this.maxRecentErrors = maxRecentErrors;
    }

    /**
     * Handles an exception from an integration flow.
     *
     * @param flowName the name of the flow where the error occurred
     * @param exception the exception to handle
     */
    public void handle(String flowName, Throwable exception) {
        log.error("Error in flow '{}': {}", flowName, exception.getMessage(), exception);

        // Update statistics
        ErrorStats stats = flowStats.computeIfAbsent(flowName, k -> new ErrorStats());
        stats.recordError(exception);

        // Record for recent errors
        recordError(flowName, exception);

        // Check for custom handlers
        invokeCustomHandlers(flowName, exception);
    }

    /**
     * Handles an IntegrationException with additional context.
     *
     * @param flowName the name of the flow
     * @param exception the integration exception
     */
    public void handle(String flowName, IntegrationException exception) {
        log.error("Integration error in flow '{}', component '{}': {}",
                flowName, exception.getComponentName(), exception.getMessage(), exception);

        ErrorStats stats = flowStats.computeIfAbsent(flowName, k -> new ErrorStats());
        stats.recordError(exception);

        recordError(flowName, exception);
        invokeCustomHandlers(flowName, exception);
    }

    /**
     * Registers a custom error handler for a specific exception type.
     *
     * @param exceptionType the exception class to handle
     * @param handler the handler to invoke
     * @param <T> the exception type
     */
    public <T extends Throwable> void registerHandler(Class<T> exceptionType,
                                                       BiConsumer<String, T> handler) {
        @SuppressWarnings("unchecked")
        BiConsumer<String, Throwable> genericHandler = (flowName, ex) ->
                handler.accept(flowName, (T) ex);
        customHandlers.put(exceptionType, genericHandler);
        log.info("Registered custom error handler for: {}", exceptionType.getSimpleName());
    }

    /**
     * Removes a custom error handler.
     *
     * @param exceptionType the exception class
     */
    public void removeHandler(Class<? extends Throwable> exceptionType) {
        customHandlers.remove(exceptionType);
    }

    /**
     * Gets error statistics for a specific flow.
     *
     * @param flowName the flow name
     * @return the error statistics, or null if no errors recorded
     */
    public ErrorStats getStats(String flowName) {
        return flowStats.get(flowName);
    }

    /**
     * Gets error statistics for all flows.
     *
     * @return an unmodifiable map of flow names to statistics
     */
    public Map<String, ErrorStats> getAllStats() {
        return Collections.unmodifiableMap(flowStats);
    }

    /**
     * Gets the total error count across all flows.
     *
     * @return the total error count
     */
    public long getTotalErrorCount() {
        return flowStats.values().stream()
                .mapToLong(ErrorStats::getErrorCount)
                .sum();
    }

    /**
     * Gets recent error records.
     *
     * @return an unmodifiable list of recent errors
     */
    public List<ErrorRecord> getRecentErrors() {
        synchronized (recentErrors) {
            return new ArrayList<>(recentErrors);
        }
    }

    /**
     * Gets recent errors for a specific flow.
     *
     * @param flowName the flow name
     * @return a list of recent errors for the flow
     */
    public List<ErrorRecord> getRecentErrors(String flowName) {
        synchronized (recentErrors) {
            return recentErrors.stream()
                    .filter(r -> r.flowName().equals(flowName))
                    .toList();
        }
    }

    /**
     * Clears all error statistics.
     */
    public void clearStats() {
        flowStats.clear();
        synchronized (recentErrors) {
            recentErrors.clear();
        }
        log.info("Cleared all error statistics");
    }

    /**
     * Clears error statistics for a specific flow.
     *
     * @param flowName the flow name
     */
    public void clearStats(String flowName) {
        flowStats.remove(flowName);
        synchronized (recentErrors) {
            recentErrors.removeIf(r -> r.flowName().equals(flowName));
        }
    }

    private void invokeCustomHandlers(String flowName, Throwable exception) {
        Class<?> exceptionClass = exception.getClass();

        // Check for exact match first
        BiConsumer<String, Throwable> handler = customHandlers.get(exceptionClass);
        if (handler != null) {
            try {
                handler.accept(flowName, exception);
            } catch (Exception e) {
                log.warn("Custom error handler failed for {}: {}", exceptionClass.getSimpleName(), e.getMessage());
            }
            return;
        }

        // Check parent classes
        for (Map.Entry<Class<? extends Throwable>, BiConsumer<String, Throwable>> entry : customHandlers.entrySet()) {
            if (entry.getKey().isAssignableFrom(exceptionClass)) {
                try {
                    entry.getValue().accept(flowName, exception);
                } catch (Exception e) {
                    log.warn("Custom error handler failed for {}: {}", entry.getKey().getSimpleName(), e.getMessage());
                }
            }
        }
    }

    private void recordError(String flowName, Throwable exception) {
        ErrorRecord record = new ErrorRecord(
                flowName,
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                Instant.now()
        );

        synchronized (recentErrors) {
            recentErrors.add(record);
            while (recentErrors.size() > maxRecentErrors) {
                recentErrors.remove(0);
            }
        }
    }

    /**
     * Error statistics for a flow.
     */
    public static class ErrorStats {
        private final AtomicLong errorCount = new AtomicLong(0);
        private final Map<String, AtomicLong> errorsByType = new ConcurrentHashMap<>();
        private volatile Instant lastErrorTime;
        private volatile String lastErrorMessage;

        /**
         * Records an error.
         *
         * @param exception the exception
         */
        void recordError(Throwable exception) {
            errorCount.incrementAndGet();
            lastErrorTime = Instant.now();
            lastErrorMessage = exception.getMessage();

            String typeName = exception.getClass().getSimpleName();
            errorsByType.computeIfAbsent(typeName, k -> new AtomicLong(0)).incrementAndGet();
        }

        /**
         * Gets the total error count.
         *
         * @return the error count
         */
        public long getErrorCount() {
            return errorCount.get();
        }

        /**
         * Gets the error count by exception type.
         *
         * @return a map of exception type names to counts
         */
        public Map<String, Long> getErrorsByType() {
            Map<String, Long> result = new ConcurrentHashMap<>();
            errorsByType.forEach((k, v) -> result.put(k, v.get()));
            return result;
        }

        /**
         * Gets the time of the last error.
         *
         * @return the last error time, or null if no errors
         */
        public Instant getLastErrorTime() {
            return lastErrorTime;
        }

        /**
         * Gets the message of the last error.
         *
         * @return the last error message, or null if no errors
         */
        public String getLastErrorMessage() {
            return lastErrorMessage;
        }

        /**
         * Resets the statistics.
         */
        public void reset() {
            errorCount.set(0);
            errorsByType.clear();
            lastErrorTime = null;
            lastErrorMessage = null;
        }
    }

    /**
     * Record of a single error occurrence.
     *
     * @param flowName the flow where the error occurred
     * @param exceptionType the type of exception
     * @param message the error message
     * @param timestamp when the error occurred
     */
    public record ErrorRecord(
            String flowName,
            String exceptionType,
            String message,
            Instant timestamp
    ) {}
}
