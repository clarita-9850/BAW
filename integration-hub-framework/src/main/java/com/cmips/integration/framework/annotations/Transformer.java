package com.cmips.integration.framework.annotations;

import com.cmips.integration.framework.support.CacheStrategy;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a transformer component.
 *
 * <p>Classes annotated with {@code @Transformer} are automatically discovered
 * and registered with the {@link com.cmips.integration.framework.core.ComponentRegistry}.
 * They should implement {@link com.cmips.integration.framework.interfaces.ITransformer}.
 *
 * <p>This annotation is itself annotated with {@code @Component}, so annotated
 * classes are automatically Spring beans.
 *
 * <p>Example usage:
 * <pre>
 * &#64;Transformer(
 *     name = "paymentTransformer",
 *     description = "Transforms raw payments to processed format",
 *     inputType = RawPayment.class,
 *     outputType = ProcessedPayment.class,
 *     cache = CacheStrategy.NONE
 * )
 * public class PaymentTransformer implements ITransformer&lt;RawPayment, ProcessedPayment&gt; {
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
public @interface Transformer {

    /**
     * The unique name of this transformer.
     *
     * <p>This name is used to reference the transformer in flow definitions
     * and for programmatic access via the ComponentRegistry.
     *
     * @return the transformer name
     */
    String name();

    /**
     * A human-readable description of this transformer.
     *
     * @return the description
     */
    String description() default "";

    /**
     * The input type accepted by this transformer.
     *
     * <p>This is used for documentation and validation purposes.
     *
     * @return the input type class
     */
    Class<?> inputType() default Object.class;

    /**
     * The output type produced by this transformer.
     *
     * <p>This is used for documentation and validation purposes.
     *
     * @return the output type class
     */
    Class<?> outputType() default Object.class;

    /**
     * The caching strategy for transformation results.
     *
     * @return the cache strategy
     */
    CacheStrategy cache() default CacheStrategy.NONE;

    /**
     * The order in which this transformer should be applied.
     *
     * <p>Lower values have higher priority.
     *
     * @return the order value
     */
    int order() default 0;

    /**
     * Whether this transformer is thread-safe.
     *
     * <p>Thread-safe transformers can be used concurrently.
     *
     * @return {@code true} if thread-safe
     */
    boolean threadSafe() default true;

    /**
     * Whether this transformer is enabled.
     *
     * @return {@code true} if enabled
     */
    boolean enabled() default true;

    /**
     * The Spring bean name for this component.
     *
     * @return the bean name
     */
    String value() default "";
}
