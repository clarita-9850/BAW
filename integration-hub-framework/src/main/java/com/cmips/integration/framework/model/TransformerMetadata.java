package com.cmips.integration.framework.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Metadata about a transformer.
 *
 * <p>This class provides descriptive information about a transformer,
 * including its name, description, input/output types, and other attributes.
 *
 * <p>Example usage:
 * <pre>
 * TransformerMetadata metadata = TransformerMetadata.builder()
 *     .name("PaymentTransformer")
 *     .description("Converts raw payment records to processed format")
 *     .inputType(RawPayment.class)
 *     .outputType(ProcessedPayment.class)
 *     .attribute("version", "2.0")
 *     .build();
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class TransformerMetadata {

    private final String name;
    private final String description;
    private final Class<?> inputType;
    private final Class<?> outputType;
    private final boolean stateless;
    private final Map<String, Object> attributes;

    private TransformerMetadata(String name, String description, Class<?> inputType,
                                 Class<?> outputType, boolean stateless,
                                 Map<String, Object> attributes) {
        this.name = name;
        this.description = description;
        this.inputType = inputType;
        this.outputType = outputType;
        this.stateless = stateless;
        this.attributes = Collections.unmodifiableMap(new HashMap<>(attributes));
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Class<?> getInputType() {
        return inputType;
    }

    public Class<?> getOutputType() {
        return outputType;
    }

    public boolean isStateless() {
        return stateless;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "TransformerMetadata{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", inputType=" + inputType +
                ", outputType=" + outputType +
                ", stateless=" + stateless +
                '}';
    }

    public static class Builder {
        private String name = "";
        private String description = "";
        private Class<?> inputType = Object.class;
        private Class<?> outputType = Object.class;
        private boolean stateless = true;
        private final Map<String, Object> attributes = new HashMap<>();

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder inputType(Class<?> inputType) {
            this.inputType = inputType;
            return this;
        }

        public Builder outputType(Class<?> outputType) {
            this.outputType = outputType;
            return this;
        }

        public Builder stateless(boolean stateless) {
            this.stateless = stateless;
            return this;
        }

        public Builder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public TransformerMetadata build() {
            return new TransformerMetadata(name, description, inputType, outputType,
                    stateless, attributes);
        }
    }
}
