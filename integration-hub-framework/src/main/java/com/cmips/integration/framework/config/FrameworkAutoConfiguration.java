package com.cmips.integration.framework.config;

import com.cmips.integration.framework.core.ComponentRegistry;
import com.cmips.integration.framework.core.ComponentScanner;
import com.cmips.integration.framework.core.ErrorHandler;
import com.cmips.integration.framework.core.FlowExecutor;
import com.cmips.integration.framework.core.IntegrationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Spring Boot auto-configuration for the Integration Hub Framework.
 *
 * <p>This configuration is automatically applied when the framework is on the
 * classpath and {@code integration.enabled=true} (default).
 *
 * <p>The configuration provides:
 * <ul>
 *   <li>Component registry and scanner for automatic component discovery</li>
 *   <li>Flow executor and integration engine for flow orchestration</li>
 *   <li>Error handler for centralized error management</li>
 *   <li>Async task executor for asynchronous flow execution</li>
 * </ul>
 *
 * <p>All beans are conditional, allowing applications to provide custom
 * implementations if needed.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 * @see IntegrationProperties
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "integration", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(IntegrationProperties.class)
@EnableAsync
@EnableScheduling
@Import(ComponentScanConfiguration.class)
public class FrameworkAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FrameworkAutoConfiguration.class);

    private final IntegrationProperties properties;

    /**
     * Creates a new auto-configuration with the given properties.
     *
     * @param properties the integration properties
     */
    public FrameworkAutoConfiguration(IntegrationProperties properties) {
        this.properties = properties;
        log.info("Integration Hub Framework auto-configuration initialized");
    }

    /**
     * Creates the error handler bean if not already defined.
     *
     * @return the error handler
     */
    @Bean
    @ConditionalOnMissingBean
    public ErrorHandler errorHandler() {
        return new ErrorHandler();
    }

    /**
     * Creates the flow executor bean if not already defined.
     *
     * @param registry the component registry
     * @param errorHandler the error handler
     * @return the flow executor
     */
    @Bean
    @ConditionalOnMissingBean
    public FlowExecutor flowExecutor(ComponentRegistry registry, ErrorHandler errorHandler) {
        return new FlowExecutor(registry, errorHandler);
    }

    /**
     * Creates the integration engine bean if not already defined.
     *
     * @param registry the component registry
     * @param executor the flow executor
     * @param errorHandler the error handler
     * @return the integration engine
     */
    @Bean
    @ConditionalOnMissingBean
    public IntegrationEngine integrationEngine(ComponentRegistry registry,
                                                 FlowExecutor executor,
                                                 ErrorHandler errorHandler) {
        return new IntegrationEngine(registry, executor, errorHandler);
    }

    /**
     * Creates an async task executor for flow execution.
     *
     * @return the task executor
     */
    @Bean(name = "integrationTaskExecutor")
    @ConditionalOnMissingBean(name = "integrationTaskExecutor")
    public Executor integrationTaskExecutor() {
        IntegrationProperties.AsyncProperties asyncProps = properties.getAsync();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncProps.getCorePoolSize());
        executor.setMaxPoolSize(asyncProps.getMaxPoolSize());
        executor.setQueueCapacity(asyncProps.getQueueCapacity());
        executor.setKeepAliveSeconds(asyncProps.getKeepAliveSeconds());
        executor.setThreadNamePrefix(asyncProps.getThreadNamePrefix());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("Integration task executor configured: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                asyncProps.getCorePoolSize(), asyncProps.getMaxPoolSize(), asyncProps.getQueueCapacity());

        return executor;
    }

    /**
     * Returns the integration properties.
     *
     * @return the properties
     */
    public IntegrationProperties getProperties() {
        return properties;
    }
}
