package com.cts.catalogservice.exception;

/**
 * Exception thrown when a user attempts to access
 * a restricted resource.
 */
public class AccessDeniedException extends RuntimeException {

    /**
     * Constructs a new AccessDeniedException.
     *
     * @param message detailed error message
     */
    public AccessDeniedException(String message) {
        super(message);
    }
}
