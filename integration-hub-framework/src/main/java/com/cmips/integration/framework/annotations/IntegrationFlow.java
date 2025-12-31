package com.cmips.integration.framework.annotations;

import org.springframework.context.annotation.Configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as an integration flow definition.
 *
 * <p>Classes annotated with {@code @IntegrationFlow} define the configuration
 * for an integration flow, specifying input sources, transformers, and output
 * destinations.
 *
 * <p>This annotation is itself annotated with {@code @Configuration}, so annotated
 * classes can contain Spring bean definitions.
 *
 * <p>Example usage:
 * <pre>
 * &#64;IntegrationFlow(
 *     name = "paymentProcessingFlow",
 *     description = "Processes payment files and uploads to external system",
 *     enabled = true,
 *     cron = "0 0 6 * * ?"
 * )
 * public class PaymentProcessingFlow {
 *
 *     &#64;Bean
 *     public FlowDefinition paymentFlowDefinition() {
 *         return FlowDefinition.builder()
 *             .inputs(List.of("paymentFileReader"))
 *             .transformers(List.of("paymentTransformer", "paymentValidator"))
 *             .outputs(List.of("sftpUploader", "databaseWriter"))
 *             .build();
 *     }
 * }
 * </pre>
 *
 * <p>Flows can also be triggered programmatically:
 * <pre>
 * &#64;Autowired
 * private IntegrationEngine engine;
 *
 * public void triggerFlow() {
 *     engine.executeFlow("paymentProcessingFlow");
 * }
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
public @interface IntegrationFlow {

    /**
     * The unique name of this integration flow.
     *
     * <p>This name is used to reference the flow for execution
     * and monitoring purposes.
     *
     * @return the flow name
     */
    String name();

    /**
     * A human-readable description of this integration flow.
     *
     * @return the description
     */
    String description() default "";

    /**
     * Whether this flow is enabled.
     *
     * <p>Disabled flows are not scheduled and cannot be triggered.
     *
     * @return {@code true} if enabled
     */
    boolean enabled() default true;

    /**
     * Cron expression for scheduled execution.
     *
     * <p>If not specified or empty, the flow is not scheduled automatically
     * and must be triggered programmatically.
     *
     * <p>Example cron expressions:
     * <ul>
     *   <li>{@code "0 0 6 * * ?"} - Daily at 6:00 AM</li>
     *   <li>{@code "0 0/30 * * * ?"} - Every 30 minutes</li>
     *   <li>{@code "0 0 * * * MON-FRI"} - Hourly on weekdays</li>
     * </ul>
     *
     * @return the cron expression
     */
    String cron() default "";

    /**
     * The maximum number of concurrent executions.
     *
     * <p>Set to 1 to prevent concurrent execution.
     *
     * @return the max concurrent executions
     */
    int maxConcurrent() default 1;

    /**
     * Timeout in milliseconds for flow execution.
     *
     * <p>Set to 0 for no timeout.
     *
     * @return the timeout in milliseconds
     */
    long timeout() default 0;

    /**
     * Whether to fail the entire flow on any component error.
     *
     * <p>If {@code false}, errors in non-required components are logged
     * but don't stop the flow.
     *
     * @return {@code true} to fail on error
     */
    boolean failOnError() default true;

    /**
     * Optional tags for categorization.
     *
     * @return array of tags
     */
    String[] tags() default {};

    /**
     * The Spring bean name for this component.
     *
     * @return the bean name
     */
    String value() default "";
}
