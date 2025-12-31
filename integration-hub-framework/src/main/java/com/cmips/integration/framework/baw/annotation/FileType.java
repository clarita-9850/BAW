package com.cmips.integration.framework.baw.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a POJO class as a File Type definition (schema).
 *
 * <p>Similar to JPA's @Entity, this annotation defines a class as a file schema
 * that can be used with the BAW Framework's repository operations.
 *
 * <p>Example usage:
 * <pre>
 * &#64;FileType(name = "payment-record", description = "Payment transaction record")
 * public class PaymentRecord {
 *     &#64;FileColumn(order = 1)
 *     &#64;FileId
 *     private String paymentId;
 *
 *     &#64;FileColumn(order = 2, format = "yyyy-MM-dd")
 *     private LocalDate paymentDate;
 *
 *     &#64;FileColumn(order = 3)
 *     private BigDecimal amount;
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
public @interface FileType {

    /**
     * Unique name for this file type.
     * Used for logging, error messages, and registry lookup.
     *
     * @return the file type name
     */
    String name();

    /**
     * Optional description of this file type.
     *
     * @return the description
     */
    String description() default "";

    /**
     * Version of this file type schema.
     * Useful for schema evolution tracking.
     *
     * @return the version string
     */
    String version() default "1.0";
}
