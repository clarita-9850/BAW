package com.cmips.integration.framework.baw.exception;

/**
 * Base exception for all BAW Framework exceptions.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class BawException extends RuntimeException {

    public BawException(String message) {
        super(message);
    }

    public BawException(String message, Throwable cause) {
        super(message, cause);
    }
}
