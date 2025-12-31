package com.cmips.integration.framework.baw.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as an identity field for deduplication purposes.
 *
 * <p>Similar to JPA's @Id, this annotation identifies fields that uniquely
 * identify a record. Multiple fields can be marked as @FileId to form
 * a composite identity.
 *
 * <p>Example usage:
 * <pre>
 * // Single field identity
 * &#64;FileId
 * &#64;FileColumn(order = 1)
 * private String paymentId;
 *
 * // Composite identity
 * &#64;FileId
 * &#64;FileColumn(order = 1)
 * private String batchNumber;
 *
 * &#64;FileId
 * &#64;FileColumn(order = 2)
 * private Integer sequenceNumber;
 * </pre>
 *
 * <p>Identity fields are used by:
 * <ul>
 *   <li>MergeBuilder.deduplicate() - to remove duplicate records</li>
 *   <li>Repository.findById() - to locate specific records</li>
 *   <li>Validation - to ensure uniqueness constraints</li>
 * </ul>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FileId {

    /**
     * Order of this field in composite identity.
     * Only relevant when multiple fields are marked as @FileId.
     *
     * @return the order in composite key (0-based)
     */
    int order() default 0;
}
