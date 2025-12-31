package com.cmips.integration.framework.baw.destination;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface as a destination definition.
 *
 * <p>Destinations represent external systems where files can be transmitted
 * (SFTP servers, HTTP endpoints, etc.).
 *
 * <p>Example usage:
 * <pre>
 * &#64;Destination(name = "bpm-sftp")
 * &#64;Sftp(
 *     host = "${sftp.bpm.host}",
 *     port = 22,
 *     remotePath = "/incoming/payments",
 *     credentials = "bpm-sftp-creds"
 * )
 * public interface BpmSftpDestination {
 * }
 *
 * &#64;Destination(name = "fin-api")
 * &#64;HttpApi(
 *     url = "${api.fin.url}/payments",
 *     method = HttpMethod.POST,
 *     contentType = "application/json"
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
public @interface Destination {

    /**
     * Unique name for this destination.
     *
     * @return the destination name
     */
    String name();

    /**
     * Description of this destination.
     *
     * @return the description
     */
    String description() default "";

    /**
     * Whether this destination is enabled.
     * Can be controlled via properties: ${destination.enabled:true}
     *
     * @return true if enabled
     */
    String enabled() default "true";
}
