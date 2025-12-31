package com.cmips.integration.framework.exception;

/**
 * Exception thrown when a connection to a source or destination fails.
 *
 * <p>This exception is thrown by input sources and output destinations
 * when they fail to establish a connection. Common causes include:
 * <ul>
 *   <li>Network connectivity issues</li>
 *   <li>Authentication failures</li>
 *   <li>Resource not found (file, server, database)</li>
 *   <li>Permission denied</li>
 *   <li>Timeout during connection attempt</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * public void connect() throws ConnectionException {
 *     try {
 *         channel = jsch.getSession(username, host, port);
 *         channel.connect(timeout);
 *     } catch (JSchException e) {
 *         throw new ConnectionException("Failed to connect to SFTP server: " + host, e);
 *     }
 * }
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConnectionException extends IntegrationException {

    private static final long serialVersionUID = 1L;

    /**
     * The host or resource that failed to connect.
     */
    private final String target;

    /**
     * The port number, if applicable.
     */
    private final int port;

    /**
     * Creates a new ConnectionException with the specified message.
     *
     * @param message the detail message
     */
    public ConnectionException(String message) {
        super(message);
        this.target = null;
        this.port = -1;
    }

    /**
     * Creates a new ConnectionException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
        this.target = null;
        this.port = -1;
    }

    /**
     * Creates a new ConnectionException with target information.
     *
     * @param message the detail message
     * @param target the host or resource that failed
     */
    public ConnectionException(String message, String target) {
        super(message);
        this.target = target;
        this.port = -1;
    }

    /**
     * Creates a new ConnectionException with full connection details.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     * @param target the host or resource that failed
     * @param port the port number
     */
    public ConnectionException(String message, Throwable cause, String target, int port) {
        super(message, cause);
        this.target = target;
        this.port = port;
    }

    /**
     * Returns the target host or resource that failed to connect.
     *
     * @return the target, or {@code null} if not set
     */
    public String getTarget() {
        return target;
    }

    /**
     * Returns the port number.
     *
     * @return the port, or -1 if not applicable
     */
    public int getPort() {
        return port;
    }

    @Override
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder(super.getDetailedMessage());
        if (target != null) {
            sb.append(" [Target: ").append(target);
            if (port > 0) {
                sb.append(":").append(port);
            }
            sb.append("]");
        }
        return sb.toString();
    }
}
