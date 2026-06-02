package com.cts.userservice.exception.customexception;
/**
 * Thrown to indicate that an authenticated principal attempted an operation
 * for which they lack the necessary permissions.
 *
 * <p>Unlike an authentication failure (which concerns identity), this
 * exception represents an authorization failure: the caller is known, but
 * is not allowed to perform the requested action or access the requested
 * resource.</p>
 *
 * <p>As an unchecked exception, it does not need to be declared in a method's
 * {@code throws} clause and is usually translated into an HTTP
 * {@code 403 Forbidden} response by a global exception handler.</p>
 *
 */
public class AccessDeniedException extends RuntimeException {
    /**
     * Constructs a new {@code AccessDeniedException} with the specified
     * detail message.
     *
     * @param message the detail message describing the denied access;
     *                retrievable later via {@link #getMessage()}
     */
    public AccessDeniedException(String message) {
        super(message);
    }
}
