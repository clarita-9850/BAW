package com.cmips.integration.framework.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines the structure of an integration flow.
 *
 * <p>A flow definition specifies the input sources, transformers, and output
 * destinations that make up an integration flow. It provides the configuration
 * needed by the {@link com.cmips.integration.framework.core.FlowExecutor} to
 * execute the flow.
 *
 * <p>Example usage:
 * <pre>
 * FlowDefinition flow = FlowDefinition.builder()
 *     .name("paymentProcessingFlow")
 *     .description("Processes payment files from multiple sources")
 *     .inputs(List.of("fileReader", "sftpReader"))
 *     .transformers(List.of("paymentValidator", "paymentTransformer"))
 *     .outputs(List.of("sftpUploader", "databaseWriter"))
 *     .failOnError(true)
 *     .build();
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class FlowDefinition {

    /**
     * The unique name of this flow.
     */
    private final String name;

    /**
     * A description of this flow.
     */
    private final String description;

    /**
     * The names of input sources.
     */
    private final List<String> inputs;

    /**
     * The names of transformers in order of execution.
     */
    private final List<String> transformers;

    /**
     * The names of output destinations.
     */
    private final List<String> outputs;

    /**
     * Whether to fail the entire flow on any error.
     */
    private final boolean failOnError;

    /**
     * Whether this flow is enabled.
     */
    private final boolean enabled;

    /**
     * Additional properties for this flow.
     */
    private final Map<String, Object> properties;

    /**
     * Private constructor used by the builder.
     */
    private FlowDefinition(String name, String description, List<String> inputs,
                           List<String> transformers, List<String> outputs,
                           boolean failOnError, boolean enabled,
                           Map<String, Object> properties) {
        this.name = name;
        this.description = description;
        this.inputs = Collections.unmodifiableList(new ArrayList<>(inputs));
        this.transformers = Collections.unmodifiableList(new ArrayList<>(transformers));
        this.outputs = Collections.unmodifiableList(new ArrayList<>(outputs));
        this.failOnError = failOnError;
        this.enabled = enabled;
        this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
    }

    /**
     * Returns a builder for creating FlowDefinition instances.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the flow name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the flow description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the list of input source names.
     *
     * @return an unmodifiable list of input names
     */
    public List<String> getInputs() {
        return inputs;
    }

    /**
     * Returns the list of transformer names.
     *
     * @return an unmodifiable list of transformer names
     */
    public List<String> getTransformers() {
        return transformers;
    }

    /**
     * Returns the list of output destination names.
     *
     * @return an unmodifiable list of output names
     */
    public List<String> getOutputs() {
        return outputs;
    }

    /**
     * Returns whether the flow should fail on any error.
     *
     * @return {@code true} if fail on error
     */
    public boolean isFailOnError() {
        return failOnError;
    }

    /**
     * Returns whether this flow is enabled.
     *
     * @return {@code true} if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the additional properties.
     *
     * @return an unmodifiable map of properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Returns a specific property value.
     *
     * @param <T> the expected type
     * @param key the property key
     * @param type the expected type class
     * @return the property value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> type) {
        Object value = properties.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * Checks if this flow has any inputs defined.
     *
     * @return {@code true} if there are inputs
     */
    public boolean hasInputs() {
        return !inputs.isEmpty();
    }

    /**
     * Checks if this flow has any transformers defined.
     *
     * @return {@code true} if there are transformers
     */
    public boolean hasTransformers() {
        return !transformers.isEmpty();
    }

    /**
     * Checks if this flow has any outputs defined.
     *
     * @return {@code true} if there are outputs
     */
    public boolean hasOutputs() {
        return !outputs.isEmpty();
    }

    @Override
    public String toString() {
        return "FlowDefinition{" +
                "name='" + name + '\'' +
                ", inputs=" + inputs +
                ", transformers=" + transformers +
                ", outputs=" + outputs +
                ", enabled=" + enabled +
                '}';
    }

    /**
     * Builder for creating FlowDefinition instances.
     */
    public static class Builder {
        private String name = "";
        private String description = "";
        private List<String> inputs = new ArrayList<>();
        private List<String> transformers = new ArrayList<>();
        private List<String> outputs = new ArrayList<>();
        private boolean failOnError = true;
        private boolean enabled = true;
        private final Map<String, Object> properties = new HashMap<>();

        private Builder() {
        }

        /**
         * Sets the flow name.
         *
         * @param name the name
         * @return this builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the flow description.
         *
         * @param description the description
         * @return this builder
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the input source names.
         *
         * @param inputs the list of input names
         * @return this builder
         */
        public Builder inputs(List<String> inputs) {
            this.inputs = inputs != null ? new ArrayList<>(inputs) : new ArrayList<>();
            return this;
        }

        /**
         * Adds an input source name.
         *
         * @param input the input name
         * @return this builder
         */
        public Builder addInput(String input) {
            this.inputs.add(input);
            return this;
        }

        /**
         * Sets the transformer names.
         *
         * @param transformers the list of transformer names
         * @return this builder
         */
        public Builder transformers(List<String> transformers) {
            this.transformers = transformers != null ? new ArrayList<>(transformers) : new ArrayList<>();
            return this;
        }

        /**
         * Adds a transformer name.
         *
         * @param transformer the transformer name
         * @return this builder
         */
        public Builder addTransformer(String transformer) {
            this.transformers.add(transformer);
            return this;
        }

        /**
         * Sets the output destination names.
         *
         * @param outputs the list of output names
         * @return this builder
         */
        public Builder outputs(List<String> outputs) {
            this.outputs = outputs != null ? new ArrayList<>(outputs) : new ArrayList<>();
            return this;
        }

        /**
         * Adds an output destination name.
         *
         * @param output the output name
         * @return this builder
         */
        public Builder addOutput(String output) {
            this.outputs.add(output);
            return this;
        }

        /**
         * Sets whether to fail on error.
         *
         * @param failOnError whether to fail on error
         * @return this builder
         */
        public Builder failOnError(boolean failOnError) {
            this.failOnError = failOnError;
            return this;
        }

        /**
         * Sets whether this flow is enabled.
         *
         * @param enabled whether enabled
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Adds a property.
         *
         * @param key the property key
         * @param value the property value
         * @return this builder
         */
        public Builder property(String key, Object value) {
            this.properties.put(key, value);
            return this;
        }

        /**
         * Builds the FlowDefinition.
         *
         * @return the built FlowDefinition
         */
        public FlowDefinition build() {
            return new FlowDefinition(name, description, inputs, transformers,
                    outputs, failOnError, enabled, properties);
        }
    }
}
