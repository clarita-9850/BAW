package com.cmips.integration.framework.core;

import com.cmips.integration.framework.exception.ConnectionException;
import com.cmips.integration.framework.exception.ReadException;
import com.cmips.integration.framework.exception.SendException;
import com.cmips.integration.framework.interfaces.IInputSource;
import com.cmips.integration.framework.interfaces.IOutputDestination;
import com.cmips.integration.framework.interfaces.ITransformer;
import com.cmips.integration.framework.model.FlowDefinition;
import com.cmips.integration.framework.model.SendResult;
import com.cmips.integration.framework.model.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ComponentRegistry.
 */
class ComponentRegistryTest {

    private ComponentRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ComponentRegistry();
    }

    @Test
    void registerInputSource_shouldRegisterSuccessfully() {
        IInputSource<String> source = createMockInputSource();

        registry.registerInputSource("testSource", source);

        assertTrue(registry.hasInputSource("testSource"));
        assertSame(source, registry.getInputSource("testSource"));
    }

    @Test
    void registerInputSource_withNullName_shouldThrowException() {
        IInputSource<String> source = createMockInputSource();

        assertThrows(IllegalArgumentException.class, () ->
                registry.registerInputSource(null, source));
    }

    @Test
    void registerInputSource_withBlankName_shouldThrowException() {
        IInputSource<String> source = createMockInputSource();

        assertThrows(IllegalArgumentException.class, () ->
                registry.registerInputSource("  ", source));
    }

    @Test
    void registerInputSource_withNullSource_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                registry.registerInputSource("test", null));
    }

    @Test
    void registerTransformer_shouldRegisterSuccessfully() {
        ITransformer<String, String> transformer = createMockTransformer();

        registry.registerTransformer("testTransformer", transformer);

        assertTrue(registry.hasTransformer("testTransformer"));
        assertSame(transformer, registry.getTransformer("testTransformer"));
    }

    @Test
    void registerOutput_shouldRegisterSuccessfully() {
        IOutputDestination<String> output = createMockOutput();

        registry.registerOutput("testOutput", output);

        assertTrue(registry.hasOutput("testOutput"));
        assertSame(output, registry.getOutput("testOutput"));
    }

    @Test
    void registerFlowDefinition_shouldRegisterSuccessfully() {
        FlowDefinition flow = FlowDefinition.builder()
                .name("testFlow")
                .description("Test flow")
                .addInput("input1")
                .addTransformer("transformer1")
                .addOutput("output1")
                .build();

        registry.registerFlowDefinition(flow);

        assertTrue(registry.hasFlow("testFlow"));
        assertSame(flow, registry.getFlowDefinition("testFlow"));
    }

    @Test
    void registerFlowDefinition_withNullDefinition_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                registry.registerFlowDefinition(null));
    }

    @Test
    void getInputSource_nonExistent_shouldReturnNull() {
        assertNull(registry.getInputSource("nonexistent"));
    }

    @Test
    void findInputSource_shouldReturnOptional() {
        IInputSource<String> source = createMockInputSource();
        registry.registerInputSource("testSource", source);

        assertTrue(registry.findInputSource("testSource").isPresent());
        assertFalse(registry.findInputSource("nonexistent").isPresent());
    }

    @Test
    void getAllInputSources_shouldReturnAllSources() {
        registry.registerInputSource("source1", createMockInputSource());
        registry.registerInputSource("source2", createMockInputSource());

        Collection<IInputSource<?>> sources = registry.getAllInputSources();

        assertEquals(2, sources.size());
    }

    @Test
    void getAllInputSources_shouldReturnUnmodifiableCollection() {
        registry.registerInputSource("source1", createMockInputSource());

        Collection<IInputSource<?>> sources = registry.getAllInputSources();

        assertThrows(UnsupportedOperationException.class, () ->
                sources.add(createMockInputSource()));
    }

    @Test
    void getInputSourceNames_shouldReturnAllNames() {
        registry.registerInputSource("source1", createMockInputSource());
        registry.registerInputSource("source2", createMockInputSource());

        Collection<String> names = registry.getInputSourceNames();

        assertEquals(2, names.size());
        assertTrue(names.contains("source1"));
        assertTrue(names.contains("source2"));
    }

    @Test
    void getComponentCounts_shouldReturnCorrectCounts() {
        registry.registerInputSource("source1", createMockInputSource());
        registry.registerInputSource("source2", createMockInputSource());
        registry.registerTransformer("transformer1", createMockTransformer());
        registry.registerOutput("output1", createMockOutput());

        Map<String, Integer> counts = registry.getComponentCounts();

        assertEquals(2, counts.get("inputSources"));
        assertEquals(1, counts.get("transformers"));
        assertEquals(1, counts.get("outputs"));
        assertEquals(0, counts.get("flows"));
    }

    @Test
    void clear_shouldRemoveAllComponents() {
        registry.registerInputSource("source1", createMockInputSource());
        registry.registerTransformer("transformer1", createMockTransformer());
        registry.registerOutput("output1", createMockOutput());

        registry.clear();

        assertFalse(registry.hasInputSource("source1"));
        assertFalse(registry.hasTransformer("transformer1"));
        assertFalse(registry.hasOutput("output1"));

        Map<String, Integer> counts = registry.getComponentCounts();
        assertEquals(0, counts.get("inputSources"));
        assertEquals(0, counts.get("transformers"));
        assertEquals(0, counts.get("outputs"));
    }

    @Test
    void registerInputSource_shouldReplaceExisting() {
        IInputSource<String> source1 = createMockInputSource();
        IInputSource<String> source2 = createMockInputSource();

        registry.registerInputSource("testSource", source1);
        registry.registerInputSource("testSource", source2);

        assertSame(source2, registry.getInputSource("testSource"));
    }

    @Test
    void getAllFlowDefinitions_shouldReturnAllFlows() {
        FlowDefinition flow1 = FlowDefinition.builder()
                .name("flow1")
                .addInput("input1")
                .addOutput("output1")
                .build();
        FlowDefinition flow2 = FlowDefinition.builder()
                .name("flow2")
                .addInput("input2")
                .addOutput("output2")
                .build();

        registry.registerFlowDefinition(flow1);
        registry.registerFlowDefinition(flow2);

        Collection<FlowDefinition> flows = registry.getAllFlowDefinitions();

        assertEquals(2, flows.size());
    }

    @Test
    void getFlowNames_shouldReturnAllFlowNames() {
        FlowDefinition flow1 = FlowDefinition.builder()
                .name("flow1")
                .addInput("input1")
                .addOutput("output1")
                .build();
        FlowDefinition flow2 = FlowDefinition.builder()
                .name("flow2")
                .addInput("input2")
                .addOutput("output2")
                .build();

        registry.registerFlowDefinition(flow1);
        registry.registerFlowDefinition(flow2);

        Collection<String> names = registry.getFlowNames();

        assertEquals(2, names.size());
        assertTrue(names.contains("flow1"));
        assertTrue(names.contains("flow2"));
    }

    // Helper methods to create mock implementations

    private IInputSource<String> createMockInputSource() {
        return new IInputSource<>() {
            @Override
            public void connect() throws ConnectionException {}

            @Override
            public boolean hasData() { return false; }

            @Override
            public List<String> read() throws ReadException { return List.of(); }

            @Override
            public void acknowledge() {}

            @Override
            public void close() {}

            @Override
            public boolean isConnected() { return false; }

            @Override
            public String getName() { return "MockSource"; }
        };
    }

    private ITransformer<String, String> createMockTransformer() {
        return new ITransformer<>() {
            @Override
            public String transform(String input) { return input; }

            @Override
            public ValidationResult validate(String input) { return ValidationResult.valid(); }

            @Override
            public String getName() { return "MockTransformer"; }
        };
    }

    private IOutputDestination<String> createMockOutput() {
        return new IOutputDestination<>() {
            @Override
            public void connect() throws ConnectionException {}

            @Override
            public SendResult send(String data) throws SendException {
                return SendResult.success("Success");
            }

            @Override
            public boolean verify(SendResult result) { return true; }

            @Override
            public void close() {}

            @Override
            public boolean isConnected() { return false; }

            @Override
            public String getName() { return "MockOutput"; }
        };
    }
}
