package com.cmips.integration.framework.core;

import com.cmips.integration.framework.interfaces.IInputSource;
import com.cmips.integration.framework.interfaces.IOutputDestination;
import com.cmips.integration.framework.interfaces.ITransformer;
import com.cmips.integration.framework.model.FlowDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for all integration framework components.
 *
 * <p>The ComponentRegistry maintains references to all discovered and registered
 * integration components including input sources, transformers, output destinations,
 * and flow definitions.
 *
 * <p>Components are registered by the {@link ComponentScanner} at application startup,
 * but can also be registered programmatically.
 *
 * <p>Example usage:
 * <pre>
 * &#64;Autowired
 * private ComponentRegistry registry;
 *
 * // Get a specific input source
 * IInputSource&lt;?&gt; source = registry.getInputSource("paymentFileReader");
 *
 * // Get all registered transformers
 * Collection&lt;ITransformer&lt;?, ?&gt;&gt; transformers = registry.getAllTransformers();
 *
 * // Register a component programmatically
 * registry.registerInputSource("customReader", new CustomFileReader());
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class ComponentRegistry {

    private static final Logger log = LoggerFactory.getLogger(ComponentRegistry.class);

    private final Map<String, IInputSource<?>> inputSources = new ConcurrentHashMap<>();
    private final Map<String, ITransformer<?, ?>> transformers = new ConcurrentHashMap<>();
    private final Map<String, IOutputDestination<?>> outputs = new ConcurrentHashMap<>();
    private final Map<String, Object> flows = new ConcurrentHashMap<>();
    private final Map<String, FlowDefinition> flowDefinitions = new ConcurrentHashMap<>();

    /**
     * Registers an input source.
     *
     * @param name the unique name for this source
     * @param source the input source instance
     */
    public void registerInputSource(String name, IInputSource<?> source) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        }

        IInputSource<?> existing = inputSources.put(name, source);
        if (existing != null) {
            log.warn("Replaced existing input source: {}", name);
        } else {
            log.info("Registered input source: {}", name);
        }
    }

    /**
     * Registers a transformer.
     *
     * @param name the unique name for this transformer
     * @param transformer the transformer instance
     */
    public void registerTransformer(String name, ITransformer<?, ?> transformer) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (transformer == null) {
            throw new IllegalArgumentException("Transformer cannot be null");
        }

        ITransformer<?, ?> existing = transformers.put(name, transformer);
        if (existing != null) {
            log.warn("Replaced existing transformer: {}", name);
        } else {
            log.info("Registered transformer: {}", name);
        }
    }

    /**
     * Registers an output destination.
     *
     * @param name the unique name for this destination
     * @param output the output destination instance
     */
    public void registerOutput(String name, IOutputDestination<?> output) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (output == null) {
            throw new IllegalArgumentException("Output cannot be null");
        }

        IOutputDestination<?> existing = outputs.put(name, output);
        if (existing != null) {
            log.warn("Replaced existing output destination: {}", name);
        } else {
            log.info("Registered output destination: {}", name);
        }
    }

    /**
     * Registers a flow configuration object.
     *
     * @param name the unique name for this flow
     * @param flow the flow configuration bean
     */
    public void registerFlow(String name, Object flow) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (flow == null) {
            throw new IllegalArgumentException("Flow cannot be null");
        }

        Object existing = flows.put(name, flow);
        if (existing != null) {
            log.warn("Replaced existing flow: {}", name);
        } else {
            log.info("Registered flow: {}", name);
        }
    }

    /**
     * Registers a flow definition.
     *
     * @param definition the flow definition
     */
    public void registerFlowDefinition(FlowDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Flow definition cannot be null");
        }
        if (definition.getName() == null || definition.getName().isBlank()) {
            throw new IllegalArgumentException("Flow definition must have a name");
        }

        flowDefinitions.put(definition.getName(), definition);
        log.info("Registered flow definition: {}", definition.getName());
    }

    /**
     * Gets an input source by name.
     *
     * @param name the source name
     * @return the input source, or null if not found
     */
    public IInputSource<?> getInputSource(String name) {
        return inputSources.get(name);
    }

    /**
     * Gets an input source by name as an Optional.
     *
     * @param name the source name
     * @return an Optional containing the source if found
     */
    public Optional<IInputSource<?>> findInputSource(String name) {
        return Optional.ofNullable(inputSources.get(name));
    }

    /**
     * Gets a transformer by name.
     *
     * @param name the transformer name
     * @return the transformer, or null if not found
     */
    public ITransformer<?, ?> getTransformer(String name) {
        return transformers.get(name);
    }

    /**
     * Gets a transformer by name as an Optional.
     *
     * @param name the transformer name
     * @return an Optional containing the transformer if found
     */
    public Optional<ITransformer<?, ?>> findTransformer(String name) {
        return Optional.ofNullable(transformers.get(name));
    }

    /**
     * Gets an output destination by name.
     *
     * @param name the destination name
     * @return the output destination, or null if not found
     */
    public IOutputDestination<?> getOutput(String name) {
        return outputs.get(name);
    }

    /**
     * Gets an output destination by name as an Optional.
     *
     * @param name the destination name
     * @return an Optional containing the destination if found
     */
    public Optional<IOutputDestination<?>> findOutput(String name) {
        return Optional.ofNullable(outputs.get(name));
    }

    /**
     * Gets a flow configuration by name.
     *
     * @param name the flow name
     * @return the flow configuration, or null if not found
     */
    public Object getFlow(String name) {
        return flows.get(name);
    }

    /**
     * Gets a flow definition by name.
     *
     * @param name the flow name
     * @return the flow definition, or null if not found
     */
    public FlowDefinition getFlowDefinition(String name) {
        return flowDefinitions.get(name);
    }

    /**
     * Gets all registered input sources.
     *
     * @return an unmodifiable collection of all input sources
     */
    public Collection<IInputSource<?>> getAllInputSources() {
        return Collections.unmodifiableCollection(inputSources.values());
    }

    /**
     * Gets all registered transformers.
     *
     * @return an unmodifiable collection of all transformers
     */
    public Collection<ITransformer<?, ?>> getAllTransformers() {
        return Collections.unmodifiableCollection(transformers.values());
    }

    /**
     * Gets all registered output destinations.
     *
     * @return an unmodifiable collection of all output destinations
     */
    public Collection<IOutputDestination<?>> getAllOutputs() {
        return Collections.unmodifiableCollection(outputs.values());
    }

    /**
     * Gets all registered flow definitions.
     *
     * @return an unmodifiable collection of all flow definitions
     */
    public Collection<FlowDefinition> getAllFlowDefinitions() {
        return Collections.unmodifiableCollection(flowDefinitions.values());
    }

    /**
     * Gets all registered input source names.
     *
     * @return an unmodifiable collection of source names
     */
    public Collection<String> getInputSourceNames() {
        return Collections.unmodifiableCollection(inputSources.keySet());
    }

    /**
     * Gets all registered transformer names.
     *
     * @return an unmodifiable collection of transformer names
     */
    public Collection<String> getTransformerNames() {
        return Collections.unmodifiableCollection(transformers.keySet());
    }

    /**
     * Gets all registered output destination names.
     *
     * @return an unmodifiable collection of destination names
     */
    public Collection<String> getOutputNames() {
        return Collections.unmodifiableCollection(outputs.keySet());
    }

    /**
     * Gets all registered flow names.
     *
     * @return an unmodifiable collection of flow names
     */
    public Collection<String> getFlowNames() {
        return Collections.unmodifiableCollection(flowDefinitions.keySet());
    }

    /**
     * Checks if an input source exists.
     *
     * @param name the source name
     * @return {@code true} if the source exists
     */
    public boolean hasInputSource(String name) {
        return inputSources.containsKey(name);
    }

    /**
     * Checks if a transformer exists.
     *
     * @param name the transformer name
     * @return {@code true} if the transformer exists
     */
    public boolean hasTransformer(String name) {
        return transformers.containsKey(name);
    }

    /**
     * Checks if an output destination exists.
     *
     * @param name the destination name
     * @return {@code true} if the destination exists
     */
    public boolean hasOutput(String name) {
        return outputs.containsKey(name);
    }

    /**
     * Checks if a flow definition exists.
     *
     * @param name the flow name
     * @return {@code true} if the flow exists
     */
    public boolean hasFlow(String name) {
        return flowDefinitions.containsKey(name);
    }

    /**
     * Returns summary statistics about registered components.
     *
     * @return a map of component type to count
     */
    public Map<String, Integer> getComponentCounts() {
        return Map.of(
                "inputSources", inputSources.size(),
                "transformers", transformers.size(),
                "outputs", outputs.size(),
                "flows", flowDefinitions.size()
        );
    }

    /**
     * Clears all registered components.
     */
    public void clear() {
        inputSources.clear();
        transformers.clear();
        outputs.clear();
        flows.clear();
        flowDefinitions.clear();
        log.info("Cleared all registered components");
    }
}
