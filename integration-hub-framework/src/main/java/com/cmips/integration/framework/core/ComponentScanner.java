package com.cmips.integration.framework.core;

import com.cmips.integration.framework.annotations.InputSource;
import com.cmips.integration.framework.annotations.IntegrationFlow;
import com.cmips.integration.framework.annotations.OutputDestination;
import com.cmips.integration.framework.annotations.Transformer;
import com.cmips.integration.framework.interfaces.IInputSource;
import com.cmips.integration.framework.interfaces.IOutputDestination;
import com.cmips.integration.framework.interfaces.ITransformer;
import com.cmips.integration.framework.model.FlowDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Scans for and registers annotated integration components.
 *
 * <p>This component listens for the Spring context refresh event and automatically
 * discovers beans annotated with:
 * <ul>
 *   <li>{@link InputSource} - Input source components</li>
 *   <li>{@link Transformer} - Transformer components</li>
 *   <li>{@link OutputDestination} - Output destination components</li>
 *   <li>{@link IntegrationFlow} - Flow configuration classes</li>
 * </ul>
 *
 * <p>Discovered components are registered with the {@link ComponentRegistry}.
 *
 * <p>The scanner also looks for {@link FlowDefinition} beans within flow
 * configuration classes and registers those as well.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class ComponentScanner implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ComponentScanner.class);

    private final ComponentRegistry registry;
    private boolean scanned = false;

    @Autowired
    public ComponentScanner(ComponentRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (scanned) {
            log.debug("Component scan already completed, skipping");
            return;
        }

        ApplicationContext context = event.getApplicationContext();
        log.info("Starting integration component scan...");

        scanInputSources(context);
        scanTransformers(context);
        scanOutputDestinations(context);
        scanIntegrationFlows(context);
        scanFlowDefinitions(context);

        scanned = true;
        logScanResults();
    }

    private void scanInputSources(ApplicationContext context) {
        Map<String, Object> beans = context.getBeansWithAnnotation(InputSource.class);
        log.debug("Found {} beans with @InputSource annotation", beans.size());

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object bean = entry.getValue();
            if (bean instanceof IInputSource<?> source) {
                InputSource annotation = bean.getClass().getAnnotation(InputSource.class);
                if (annotation != null && annotation.enabled()) {
                    String name = annotation.name();
                    if (name.isBlank()) {
                        name = entry.getKey();
                    }
                    registry.registerInputSource(name, source);
                }
            } else {
                log.warn("Bean {} has @InputSource annotation but does not implement IInputSource",
                        entry.getKey());
            }
        }
    }

    private void scanTransformers(ApplicationContext context) {
        Map<String, Object> beans = context.getBeansWithAnnotation(Transformer.class);
        log.debug("Found {} beans with @Transformer annotation", beans.size());

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object bean = entry.getValue();
            if (bean instanceof ITransformer<?, ?> transformer) {
                Transformer annotation = bean.getClass().getAnnotation(Transformer.class);
                if (annotation != null && annotation.enabled()) {
                    String name = annotation.name();
                    if (name.isBlank()) {
                        name = entry.getKey();
                    }
                    registry.registerTransformer(name, transformer);
                }
            } else {
                log.warn("Bean {} has @Transformer annotation but does not implement ITransformer",
                        entry.getKey());
            }
        }
    }

    private void scanOutputDestinations(ApplicationContext context) {
        Map<String, Object> beans = context.getBeansWithAnnotation(OutputDestination.class);
        log.debug("Found {} beans with @OutputDestination annotation", beans.size());

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object bean = entry.getValue();
            if (bean instanceof IOutputDestination<?> output) {
                OutputDestination annotation = bean.getClass().getAnnotation(OutputDestination.class);
                if (annotation != null && annotation.enabled()) {
                    String name = annotation.name();
                    if (name.isBlank()) {
                        name = entry.getKey();
                    }
                    registry.registerOutput(name, output);
                }
            } else {
                log.warn("Bean {} has @OutputDestination annotation but does not implement IOutputDestination",
                        entry.getKey());
            }
        }
    }

    private void scanIntegrationFlows(ApplicationContext context) {
        Map<String, Object> beans = context.getBeansWithAnnotation(IntegrationFlow.class);
        log.debug("Found {} beans with @IntegrationFlow annotation", beans.size());

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object bean = entry.getValue();
            IntegrationFlow annotation = bean.getClass().getAnnotation(IntegrationFlow.class);
            if (annotation != null && annotation.enabled()) {
                String name = annotation.name();
                if (name.isBlank()) {
                    name = entry.getKey();
                }
                registry.registerFlow(name, bean);
            }
        }
    }

    private void scanFlowDefinitions(ApplicationContext context) {
        Map<String, FlowDefinition> beans = context.getBeansOfType(FlowDefinition.class);
        log.debug("Found {} FlowDefinition beans", beans.size());

        for (Map.Entry<String, FlowDefinition> entry : beans.entrySet()) {
            FlowDefinition definition = entry.getValue();
            if (definition.isEnabled()) {
                registry.registerFlowDefinition(definition);
            }
        }
    }

    private void logScanResults() {
        Map<String, Integer> counts = registry.getComponentCounts();
        log.info("Component scan completed. Registered: {} input sources, {} transformers, " +
                        "{} outputs, {} flows",
                counts.get("inputSources"),
                counts.get("transformers"),
                counts.get("outputs"),
                counts.get("flows"));
    }

    /**
     * Returns whether the scan has been completed.
     *
     * @return {@code true} if scan is complete
     */
    public boolean isScanned() {
        return scanned;
    }

    /**
     * Forces a rescan of components.
     *
     * @param context the application context
     */
    public void rescan(ApplicationContext context) {
        log.info("Forcing component rescan...");
        registry.clear();
        scanned = false;
        onApplicationEvent(new ContextRefreshedEvent(context));
    }
}
