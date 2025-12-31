package com.cmips.integration.framework.baw.destination;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures an SFTP destination.
 *
 * <p>Use this annotation alongside @Destination to define an SFTP server
 * where files can be transmitted.
 *
 * <p>Example usage:
 * <pre>
 * &#64;Destination(name = "bpm-sftp")
 * &#64;Sftp(
 *     host = "${sftp.bpm.host}",
 *     port = 22,
 *     remotePath = "/incoming/payments",
 *     credentials = "bpm-sftp-creds",
 *     tempSuffix = ".tmp",
 *     createDirectory = true
 * )
 * public interface BpmSftpDestination {
 * }
 * </pre>
 *
 * <p>Property placeholders (${...}) are resolved from:
 * <ul>
 *   <li>System properties</li>
 *   <li>Environment variables</li>
 *   <li>Application configuration</li>
 * </ul>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sftp {

    /**
     * SFTP server hostname.
     * Supports property placeholders: ${sftp.host}
     *
     * @return the hostname
     */
    String host();

    /**
     * SFTP server port.
     *
     * @return the port number
     */
    int port() default 22;

    /**
     * Remote path (directory) on the SFTP server.
     * Supports property placeholders.
     *
     * @return the remote path
     */
    String remotePath();

    /**
     * Credentials reference name.
     * The actual credentials are provided via CredentialsProvider.
     *
     * @return the credentials reference
     */
    String credentials();

    /**
     * Connection timeout in milliseconds.
     *
     * @return the connection timeout
     */
    int connectionTimeout() default 30000;

    /**
     * Whether to automatically create the remote directory if it doesn't exist.
     *
     * @return true to create directory
     */
    boolean createDirectory() default false;

    /**
     * Temporary file suffix for atomic uploads.
     * Files are uploaded with this suffix and renamed upon completion.
     * Empty string disables atomic uploads.
     *
     * @return the temporary suffix
     */
    String tempSuffix() default ".tmp";

    /**
     * Known hosts file path for SSH host key verification.
     * Empty string disables strict host key checking (not recommended for production).
     *
     * @return the known hosts file path
     */
    String knownHosts() default "";
}
