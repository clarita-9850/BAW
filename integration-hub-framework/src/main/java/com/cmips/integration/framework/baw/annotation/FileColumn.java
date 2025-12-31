package com.cmips.integration.framework.baw.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as a column in the file type definition.
 *
 * <p>Similar to JPA's @Column, this annotation defines how a field maps
 * to a column in various file formats (CSV, Fixed-Width, XML, JSON).
 *
 * <p>Example usage:
 * <pre>
 * &#64;FileColumn(order = 1, name = "PAYMENT_ID")
 * private String paymentId;
 *
 * &#64;FileColumn(order = 2, length = 10, padChar = '0', alignment = Alignment.RIGHT)
 * private String accountNumber;
 *
 * &#64;FileColumn(order = 3, format = "yyyy-MM-dd", nullable = false)
 * private LocalDate transactionDate;
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FileColumn {

    /**
     * Column name (defaults to field name).
     * Used as header name in CSV, element/attribute name in XML.
     *
     * @return the column name
     */
    String name() default "";

    /**
     * Position order in the record (1-based).
     * Required for fixed-width and ordering in delimited formats.
     *
     * @return the column order
     */
    int order();

    /**
     * Length for fixed-width formats.
     * -1 means variable length (not applicable for fixed-width).
     *
     * @return the column length
     */
    int length() default -1;

    /**
     * Padding character for fixed-width formats when value is shorter than length.
     *
     * @return the padding character
     */
    char padChar() default ' ';

    /**
     * Alignment for fixed-width formats.
     *
     * @return the alignment
     */
    Alignment alignment() default Alignment.LEFT;

    /**
     * Whether this column can be null.
     *
     * @return true if nullable
     */
    boolean nullable() default true;

    /**
     * Format pattern for dates and numbers.
     * For dates: "yyyy-MM-dd", "MM/dd/yyyy", etc.
     * For numbers: "#,##0.00", "0.00", etc.
     *
     * @return the format pattern
     */
    String format() default "";

    /**
     * Default value when the field is null during write or missing during read.
     *
     * @return the default value as string
     */
    String defaultValue() default "";

    /**
     * For XML format: whether this should be an attribute rather than element.
     *
     * @return true if XML attribute
     */
    boolean xmlAttribute() default false;

    /**
     * For XML format: XPath expression for nested elements.
     *
     * @return the XPath
     */
    String xpath() default "";

    /**
     * Alignment options for fixed-width formats.
     */
    enum Alignment {
        LEFT, RIGHT, CENTER
    }
}
