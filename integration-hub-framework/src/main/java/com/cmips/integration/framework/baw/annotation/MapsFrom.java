package com.cmips.integration.framework.baw.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a field mapping from another File Type for conversion operations.
 *
 * <p>This annotation enables type-safe conversion between different file types
 * by specifying how fields map from source types to target types.
 *
 * <p>Example usage:
 * <pre>
 * // Simple field mapping
 * &#64;MapsFrom(source = HrmPayment.class, field = "employeeId")
 * private String empId;
 *
 * // Multiple source types (for unified target type)
 * &#64;MapsFrom(source = HrmPayment.class, field = "employeeId")
 * &#64;MapsFrom(source = FinPayment.class, field = "personId")
 * private String personIdentifier;
 *
 * // With transformer
 * &#64;MapsFrom(source = HrmPayment.class, field = "salary", transformer = CentsToDollarsTransformer.class)
 * private BigDecimal amount;
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(MapsFrom.List.class)
public @interface MapsFrom {

    /**
     * The source File Type class.
     *
     * @return the source type
     */
    Class<?> source();

    /**
     * The field name in the source type.
     *
     * @return the source field name
     */
    String field();

    /**
     * Optional transformer class for value transformation.
     * Must implement FieldTransformer interface.
     *
     * @return the transformer class
     */
    Class<? extends FieldTransformer<?, ?>> transformer() default NoOpTransformer.class;

    /**
     * Container for repeatable @MapsFrom annotations.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        MapsFrom[] value();
    }

    /**
     * No-op transformer (identity transformation).
     */
    final class NoOpTransformer implements FieldTransformer<Object, Object> {
        @Override
        public Object transform(Object value) {
            return value;
        }
    }
}
