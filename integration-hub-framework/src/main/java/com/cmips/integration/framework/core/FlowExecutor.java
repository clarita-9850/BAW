package com.cmips.integration.framework.core;

import com.cmips.integration.framework.exception.IntegrationException;
import com.cmips.integration.framework.interfaces.IInputSource;
import com.cmips.integration.framework.interfaces.IOutputDestination;
import com.cmips.integration.framework.interfaces.ITransformer;
import com.cmips.integration.framework.model.FlowDefinition;
import com.cmips.integration.framework.model.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes integration flows.
 *
 * <p>The FlowExecutor retrieves components from the registry and orchestrates
 * the execution of input -> transform -> output pipelines.
 *
 * <p>Example usage:
 * <pre>
 * &#64;Autowired
 * private FlowExecutor executor;
 *
 * // Execute a flow by name
 * FlowResult result = executor.executeFlow("paymentProcessingFlow");
 *
 * // Check results
 * if (result.isSuccess()) {
 *     log.info("Processed {} records in {}ms", result.getRecordCount(), result.getDurationMs());
 * } else {
 *     log.error("Flow failed: {}", result.getErrorMessage());
 * }
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class FlowExecutor {

    private static final Logger log = LoggerFactory.getLogger(FlowExecutor.class);

    private final ComponentRegistry registry;
    private final ErrorHandler errorHandler;

    @Autowired
    public FlowExecutor(ComponentRegistry registry, ErrorHandler errorHandler) {
        this.registry = registry;
        this.errorHandler = errorHandler;
    }

    /**
     * Executes an integration flow by name.
     *
     * @param flowName the name of the flow to execute
     * @return the flow execution result
     */
    public FlowResult executeFlow(String flowName) {
        FlowDefinition definition = registry.getFlowDefinition(flowName);
        if (definition == null) {
            return FlowResult.failure(flowName, "Flow definition not found: " + flowName);
        }

        return executeFlow(definition);
    }

    /**
     * Executes an integration flow from a definition.
     *
     * @param definition the flow definition
     * @return the flow execution result
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public FlowResult executeFlow(FlowDefinition definition) {
        String flowName = definition.getName();
        Instant startTime = Instant.now();
        log.info("Starting flow execution: {}", flowName);

        try {
            // Phase 1: Read from all input sources
            List<Object> allData = new ArrayList<>();
            for (String inputName : definition.getInputs()) {
                IInputSource<?> source = registry.getInputSource(inputName);
                if (source == null) {
                    throw new IntegrationException("Input source not found: " + inputName,
                            null, inputName, flowName);
                }

                try {
                    source.connect();
                    if (source.hasData()) {
                        List<?> data = source.read();
                        allData.addAll(data);
                        log.debug("Read {} records from {}", data.size(), inputName);
                    }
                } finally {
                    source.close();
                }
            }

            if (allData.isEmpty()) {
                log.info("No data to process for flow: {}", flowName);
                return FlowResult.success(flowName, 0, Duration.between(startTime, Instant.now()));
            }

            // Phase 2: Apply transformers in sequence
            List<Object> transformedData = allData;
            for (String transformerName : definition.getTransformers()) {
                ITransformer transformer = registry.getTransformer(transformerName);
                if (transformer == null) {
                    throw new IntegrationException("Transformer not found: " + transformerName,
                            null, transformerName, flowName);
                }

                List<Object> newData = new ArrayList<>();
                for (Object item : transformedData) {
                    Object transformed = transformer.transform(item);
                    newData.add(transformed);
                }
                transformedData = newData;
                log.debug("Transformed {} records with {}", transformedData.size(), transformerName);
            }

            // Phase 3: Send to all output destinations
            int successCount = 0;
            List<String> outputErrors = new ArrayList<>();

            for (String outputName : definition.getOutputs()) {
                IOutputDestination output = registry.getOutput(outputName);
                if (output == null) {
                    String error = "Output destination not found: " + outputName;
                    if (definition.isFailOnError()) {
                        throw new IntegrationException(error, null, outputName, flowName);
                    }
                    outputErrors.add(error);
                    continue;
                }

                try {
                    output.connect();
                    SendResult result = output.sendBatch(transformedData);

                    if (result.isSuccess()) {
                        successCount++;
                        log.debug("Sent {} records to {}", transformedData.size(), outputName);
                    } else if (definition.isFailOnError()) {
                        throw new IntegrationException("Output failed: " + result.getMessage(),
                                null, outputName, flowName);
                    } else {
                        outputErrors.add(outputName + ": " + result.getMessage());
                    }
                } finally {
                    output.close();
                }
            }

            // Acknowledge sources on success
            if (outputErrors.isEmpty()) {
                for (String inputName : definition.getInputs()) {
                    IInputSource<?> source = registry.getInputSource(inputName);
                    if (source != null) {
                        source.acknowledge();
                    }
                }
            }

            Duration duration = Duration.between(startTime, Instant.now());
            log.info("Flow {} completed. Processed {} records in {}ms",
                    flowName, transformedData.size(), duration.toMillis());

            if (outputErrors.isEmpty()) {
                return FlowResult.success(flowName, transformedData.size(), duration);
            } else {
                return FlowResult.partial(flowName, transformedData.size(), duration,
                        String.join("; ", outputErrors));
            }

        } catch (IntegrationException e) {
            errorHandler.handle(flowName, e);
            Duration duration = Duration.between(startTime, Instant.now());
            return FlowResult.failure(flowName, e.getMessage(), duration);
        } catch (Exception e) {
            errorHandler.handle(flowName, e);
            Duration duration = Duration.between(startTime, Instant.now());
            return FlowResult.failure(flowName, "Unexpected error: " + e.getMessage(), duration);
        }
    }

    /**
     * Validates that a flow can be executed.
     *
     * @param flowName the flow name
     * @return a list of validation errors, empty if valid
     */
    public List<String> validateFlow(String flowName) {
        List<String> errors = new ArrayList<>();

        FlowDefinition definition = registry.getFlowDefinition(flowName);
        if (definition == null) {
            errors.add("Flow definition not found: " + flowName);
            return errors;
        }

        // Check inputs
        for (String inputName : definition.getInputs()) {
            if (!registry.hasInputSource(inputName)) {
                errors.add("Input source not found: " + inputName);
            }
        }

        // Check transformers
        for (String transformerName : definition.getTransformers()) {
            if (!registry.hasTransformer(transformerName)) {
                errors.add("Transformer not found: " + transformerName);
            }
        }

        // Check outputs
        for (String outputName : definition.getOutputs()) {
            if (!registry.hasOutput(outputName)) {
                errors.add("Output destination not found: " + outputName);
            }
        }

        return errors;
    }

    /**
     * Represents the result of a flow execution.
     */
    public static class FlowResult {
        private final String flowName;
        private final boolean success;
        private final boolean partial;
        private final int recordCount;
        private final Duration duration;
        private final String message;

        private FlowResult(String flowName, boolean success, boolean partial, int recordCount,
                           Duration duration, String message) {
            this.flowName = flowName;
            this.success = success;
            this.partial = partial;
            this.recordCount = recordCount;
            this.duration = duration;
            this.message = message;
        }

        public static FlowResult success(String flowName, int recordCount, Duration duration) {
            return new FlowResult(flowName, true, false, recordCount, duration, "Success");
        }

        public static FlowResult partial(String flowName, int recordCount, Duration duration,
                                          String warnings) {
            return new FlowResult(flowName, false, true, recordCount, duration, warnings);
        }

        public static FlowResult failure(String flowName, String error) {
            return new FlowResult(flowName, false, false, 0, Duration.ZERO, error);
        }

        public static FlowResult failure(String flowName, String error, Duration duration) {
            return new FlowResult(flowName, false, false, 0, duration, error);
        }

        public String getFlowName() {
            return flowName;
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isPartial() {
            return partial;
        }

        public int getRecordCount() {
            return recordCount;
        }

        public Duration getDuration() {
            return duration;
        }

        public long getDurationMs() {
            return duration.toMillis();
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "FlowResult{" +
                    "flowName='" + flowName + '\'' +
                    ", success=" + success +
                    ", recordCount=" + recordCount +
                    ", durationMs=" + getDurationMs() +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
