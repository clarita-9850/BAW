package com.cmips.integration.framework;

/**
 * Marker class for the Integration Hub Framework.
 *
 * <p>This class serves as a reference point for Spring Boot auto-configuration
 * and component scanning. It identifies the root package of the framework.
 *
 * <p>The Integration Hub Framework provides a flexible, extensible architecture
 * for enterprise batch processing and data integration, supporting:
 * <ul>
 *   <li>Multiple input sources (files, SFTP, REST APIs, databases)</li>
 *   <li>Configurable data transformations</li>
 *   <li>Multiple output destinations</li>
 *   <li>Built-in retry and circuit breaker patterns</li>
 *   <li>Comprehensive error handling</li>
 * </ul>
 *
 * <p>Example usage in a Spring Boot application:
 * <pre>
 * &#64;SpringBootApplication
 * &#64;Import(IntegrationHubFramework.class)
 * public class MyIntegrationApp {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyIntegrationApp.class, args);
 *     }
 * }
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class IntegrationHubFramework {

    /**
     * Framework version constant.
     */
    public static final String VERSION = "1.0.0";

    /**
     * Framework name constant.
     */
    public static final String NAME = "Integration Hub Framework";

    /**
     * Base package for component scanning.
     */
    public static final String BASE_PACKAGE = "com.cmips.integration.framework";

    /**
     * Private constructor to prevent instantiation.
     */
    private IntegrationHubFramework() {
        throw new UnsupportedOperationException("Marker class - do not instantiate");
    }

    /**
     * Returns the framework version.
     *
     * @return the version string
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * Returns the framework name.
     *
     * @return the framework name
     */
    public static String getName() {
        return NAME;
    }
}
