package com.cmips.integration.framework.baw.destination;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures an HTTP API destination.
 *
 * <p>Use this annotation alongside @Destination to define an HTTP endpoint
 * where files/data can be transmitted.
 *
 * <p>Example usage:
 * <pre>
 * &#64;Destination(name = "fin-api")
 * &#64;HttpApi(
 *     url = "${api.fin.url}/payments",
 *     method = HttpMethod.POST,
 *     contentType = "application/json",
 *     authentication = "fin-api-oauth",
 *     headers = {"X-Request-Id: ${requestId}", "X-Batch-Number: ${batchNumber}"}
 * )
 * public interface FinApiDestination {
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
public @interface HttpApi {

    /**
     * HTTP URL endpoint.
     * Supports property placeholders: ${api.url}
     *
     * @return the URL
     */
    String url();

    /**
     * HTTP method.
     *
     * @return the HTTP method
     */
    HttpMethod method() default HttpMethod.POST;

    /**
     * Content-Type header value.
     *
     * @return the content type
     */
    String contentType() default "application/json";

    /**
     * Additional HTTP headers.
     * Format: "Header-Name: value" or "Header-Name: ${placeholder}"
     *
     * @return the headers
     */
    String[] headers() default {};

    /**
     * Authentication reference name.
     * The actual credentials are provided via CredentialsProvider.
     *
     * @return the authentication reference
     */
    String authentication() default "";

    /**
     * Connection timeout in milliseconds.
     *
     * @return the connection timeout
     */
    int connectionTimeout() default 30000;

    /**
     * Read timeout in milliseconds.
     *
     * @return the read timeout
     */
    int readTimeout() default 60000;

    /**
     * Whether to use multipart/form-data for file upload.
     *
     * @return true for multipart upload
     */
    boolean multipart() default false;

    /**
     * Multipart field name for the file content.
     * Only used when multipart = true.
     *
     * @return the field name
     */
    String multipartFieldName() default "file";

    /**
     * HTTP methods.
     */
    enum HttpMethod {
        GET, POST, PUT, PATCH, DELETE
    }
}
