package com.cmips.integration.framework.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as an input source component.
 *
 * <p>Classes annotated with {@code @InputSource} are automatically discovered
 * and registered with the {@link com.cmips.integration.framework.core.ComponentRegistry}.
 * They should implement {@link com.cmips.integration.framework.interfaces.IInputSource}.
 *
 * <p>This annotation is itself annotated with {@code @Component}, so annotated
 * classes are automatically Spring beans.
 *
 * <p>Example usage:
 * <pre>
 * &#64;InputSource(
 *     name = "paymentFileReader",
 *     description = "Reads payment files from the input directory",
 *     order = 1,
 *     enabled = true
 * )
 * public class PaymentFileReader implements IInputSource&lt;Payment&gt; {
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
public @interface InputSource {

    /**
     * The unique name of this input source.
     *
     * <p>This name is used to reference the source in flow definitions
     * and for programmatic access via the ComponentRegistry.
     *
     * @return the source name
     */
    String name();

    /**
     * A human-readable description of this input source.
     *
     * @return the description
     */
    String description() default "";

    /**
     * The processing order when multiple sources are used.
     *
     * <p>Lower values have higher priority. Sources with the same order
     * value are processed in an unspecified order.
     *
     * @return the order value
     */
    int order() default 0;

    /**
     * Whether this source is enabled.
     *
     * <p>Disabled sources are not processed in integration flows.
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
     * <p>If not specified, defaults to the class name with first letter lowercase.
     *
     * @return the bean name
     */
    String value() default "";
}
