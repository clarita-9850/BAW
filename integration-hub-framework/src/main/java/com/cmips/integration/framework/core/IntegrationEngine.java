package com.cmips.integration.framework.core;

import com.cmips.integration.framework.model.FlowDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Main orchestration engine for the integration framework.
 *
 * <p>The IntegrationEngine manages overall flow execution, provides an API
 * for manual flow triggering, and coordinates scheduled flows.
 *
 * <p>Example usage:
 * <pre>
 * &#64;Autowired
 * private IntegrationEngine engine;
 *
 * // Execute a flow synchronously
 * FlowResult result = engine.executeFlow("paymentFlow");
 *
 * // Execute a flow asynchronously
 * Future&lt;FlowResult&gt; future = engine.executeFlowAsync("paymentFlow");
 *
 * // Check if a flow is currently running
 * boolean running = engine.isFlowRunning("paymentFlow");
 *
 * // Get all running flows
 * Collection&lt;String&gt; runningFlows = engine.getRunningFlows();
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class IntegrationEngine {

    private static final Logger log = LoggerFactory.getLogger(IntegrationEngine.class);

    private final ComponentRegistry registry;
    private final FlowExecutor executor;
    private final ErrorHandler errorHandler;
    private final ExecutorService asyncExecutor;
    private final Map<String, Future<FlowExecutor.FlowResult>> runningFlows;

    @Autowired
    public IntegrationEngine(ComponentRegistry registry, FlowExecutor executor,
                               ErrorHandler errorHandler) {
        this.registry = registry;
        this.executor = executor;
        this.errorHandler = errorHandler;
        this.asyncExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setName("integration-flow-" + t.getId());
            t.setDaemon(true);
            return t;
        });
        this.runningFlows = new ConcurrentHashMap<>();
    }

    /**
     * Executes a flow synchronously.
     *
     * @param flowName the name of the flow to execute
     * @return the flow execution result
     */
    public FlowExecutor.FlowResult executeFlow(String flowName) {
        log.info("Executing flow: {}", flowName);

        if (!registry.hasFlow(flowName) && registry.getFlowDefinition(flowName) == null) {
            return FlowExecutor.FlowResult.failure(flowName, "Flow not found: " + flowName);
        }

        return executor.executeFlow(flowName);
    }

    /**
     * Executes a flow asynchronously.
     *
     * @param flowName the name of the flow to execute
     * @return a Future containing the flow result
     */
    public Future<FlowExecutor.FlowResult> executeFlowAsync(String flowName) {
        log.info("Submitting async flow execution: {}", flowName);

        Future<FlowExecutor.FlowResult> future = asyncExecutor.submit(() -> {
            try {
                return executeFlow(flowName);
            } finally {
                runningFlows.remove(flowName);
            }
        });

        runningFlows.put(flowName, future);
        return future;
    }

    /**
     * Executes all enabled flows.
     *
     * @return a map of flow names to their results
     */
    public Map<String, FlowExecutor.FlowResult> executeAllFlows() {
        log.info("Executing all enabled flows");

        Map<String, FlowExecutor.FlowResult> results = new ConcurrentHashMap<>();
        Collection<FlowDefinition> flows = registry.getAllFlowDefinitions();

        for (FlowDefinition flow : flows) {
            if (flow.isEnabled()) {
                FlowExecutor.FlowResult result = executeFlow(flow.getName());
                results.put(flow.getName(), result);
            }
        }

        return results;
    }

    /**
     * Checks if a flow is currently running.
     *
     * @param flowName the flow name
     * @return {@code true} if the flow is running
     */
    public boolean isFlowRunning(String flowName) {
        Future<?> future = runningFlows.get(flowName);
        return future != null && !future.isDone();
    }

    /**
     * Gets the names of all currently running flows.
     *
     * @return collection of running flow names
     */
    public Collection<String> getRunningFlows() {
        return runningFlows.entrySet().stream()
                .filter(e -> !e.getValue().isDone())
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Cancels a running flow.
     *
     * @param flowName the flow name
     * @return {@code true} if the flow was cancelled
     */
    public boolean cancelFlow(String flowName) {
        Future<?> future = runningFlows.get(flowName);
        if (future != null && !future.isDone()) {
            boolean cancelled = future.cancel(true);
            if (cancelled) {
                log.info("Cancelled flow: {}", flowName);
            }
            return cancelled;
        }
        return false;
    }

    /**
     * Validates a flow before execution.
     *
     * @param flowName the flow name
     * @return a list of validation errors, empty if valid
     */
    public java.util.List<String> validateFlow(String flowName) {
        return executor.validateFlow(flowName);
    }

    /**
     * Gets statistics about the engine.
     *
     * @return a map of statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Integer> componentCounts = registry.getComponentCounts();
        return Map.of(
                "inputSources", componentCounts.get("inputSources"),
                "transformers", componentCounts.get("transformers"),
                "outputs", componentCounts.get("outputs"),
                "flows", componentCounts.get("flows"),
                "runningFlows", runningFlows.size()
        );
    }

    /**
     * Shuts down the engine gracefully.
     */
    public void shutdown() {
        log.info("Shutting down integration engine...");

        asyncExecutor.shutdown();
        try {
            if (!asyncExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("Integration engine shut down");
    }

    /**
     * Checks if the engine is ready to execute flows.
     *
     * @return {@code true} if ready
     */
    public boolean isReady() {
        return !asyncExecutor.isShutdown() && !asyncExecutor.isTerminated();
    }

    /**
     * Gets the component registry.
     *
     * @return the registry
     */
    public ComponentRegistry getRegistry() {
        return registry;
    }

    /**
     * Gets the flow executor.
     *
     * @return the executor
     */
    public FlowExecutor getFlowExecutor() {
        return executor;
    }
}
