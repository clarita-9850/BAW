package com.cmips.integration.framework.config;

import com.cmips.integration.framework.core.ComponentRegistry;
import com.cmips.integration.framework.core.ComponentScanner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for component scanning in the integration framework.
 *
 * <p>This configuration ensures that the ComponentRegistry and ComponentScanner
 * beans are available for the framework to discover and register integration
 * components at application startup.
 *
 * <p>Both beans are conditional, allowing applications to provide custom
 * implementations if needed.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class ComponentScanConfiguration {

    /**
     * Creates the component registry bean if not already defined.
     *
     * @return the component registry
     */
    @Bean
    @ConditionalOnMissingBean
    public ComponentRegistry componentRegistry() {
        return new ComponentRegistry();
    }

    /**
     * Creates the component scanner bean if not already defined.
     *
     * @param registry the component registry
     * @return the component scanner
     */
    @Bean
    @ConditionalOnMissingBean
    public ComponentScanner componentScanner(ComponentRegistry registry) {
        return new ComponentScanner(registry);
    }
}
