package com.cmips.integration.framework.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as an output destination component.
 *
 * <p>Classes annotated with {@code @OutputDestination} are automatically discovered
 * and registered with the {@link com.cmips.integration.framework.core.ComponentRegistry}.
 * They should implement {@link com.cmips.integration.framework.interfaces.IOutputDestination}.
 *
 * <p>This annotation is itself annotated with {@code @Component}, so annotated
 * classes are automatically Spring beans.
 *
 * <p>Example usage:
 * <pre>
 * &#64;OutputDestination(
 *     name = "sftpUploader",
 *     description = "Uploads processed files to SFTP server",
 *     retry = 3,
 *     required = true
 * )
 * public class SftpUploader implements IOutputDestination&lt;ProcessedFile&gt; {
 *     // Implementation...
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
@Component
public @interface OutputDestination {

    /**
     * The unique name of this output destination.
     *
     * <p>This name is used to reference the destination in flow definitions
     * and for programmatic access via the ComponentRegistry.
     *
     * @return the destination name
     */
    String name();

    /**
     * A human-readable description of this output destination.
     *
     * @return the description
     */
    String description() default "";

    /**
     * The number of retry attempts for failed send operations.
     *
     * <p>Set to 0 to disable retries.
     *
     * @return the retry count
     */
    int retry() default 0;

    /**
     * Whether this destination is required for flow completion.
     *
     * <p>If {@code true}, the flow fails if sending to this destination fails.
     * If {@code false}, the flow continues even if this destination fails.
     *
     * @return {@code true} if required
     */
    boolean required() default true;

    /**
     * The processing order when multiple destinations are used.
     *
     * <p>Lower values have higher priority.
     *
     * @return the order value
     */
    int order() default 0;

    /**
     * Whether this destination is enabled.
     *
     * @return {@code true} if enabled
     */
    boolean enabled() default true;

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
