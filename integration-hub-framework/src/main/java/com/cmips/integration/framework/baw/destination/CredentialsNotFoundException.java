package com.cmips.integration.framework.baw.destination;

/**
 * Exception thrown when credentials cannot be found.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class CredentialsNotFoundException extends RuntimeException {

    public CredentialsNotFoundException(String message) {
        super(message);
    }

    public CredentialsNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
