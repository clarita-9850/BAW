package com.cmips.integration.framework.baw.exception;

/**
 * Exception thrown when destination operations fail.
 *
 * <p>This exception is thrown when:
 * <ul>
 *   <li>Connection to destination fails</li>
 *   <li>File transmission fails</li>
 *   <li>Destination is not properly configured</li>
 *   <li>Authentication fails</li>
 * </ul>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class DestinationException extends BawException {

    private final String destinationName;
    private final String host;
    private final Integer retryCount;

    public DestinationException(String message) {
        super(message);
        this.destinationName = null;
        this.host = null;
        this.retryCount = null;
    }

    public DestinationException(String message, Throwable cause) {
        super(message, cause);
        this.destinationName = null;
        this.host = null;
        this.retryCount = null;
    }

    public DestinationException(String destinationName, String message) {
        super("Destination [" + destinationName + "]: " + message);
        this.destinationName = destinationName;
        this.host = null;
        this.retryCount = null;
    }

    public DestinationException(String destinationName, String message, Throwable cause) {
        super("Destination [" + destinationName + "]: " + message, cause);
        this.destinationName = destinationName;
        this.host = null;
        this.retryCount = null;
    }

    public DestinationException(String destinationName, String host, String message, Throwable cause) {
        super("Destination [" + destinationName + "] at " + host + ": " + message, cause);
        this.destinationName = destinationName;
        this.host = host;
        this.retryCount = null;
    }

    public DestinationException(String destinationName, String host, int retryCount, String message, Throwable cause) {
        super("Destination [" + destinationName + "] at " + host + " (after " + retryCount +
              " retries): " + message, cause);
        this.destinationName = destinationName;
        this.host = host;
        this.retryCount = retryCount;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public String getHost() {
        return host;
    }

    public Integer getRetryCount() {
        return retryCount;
    }
}
