package com.cts.userservice.exception.customexception;
/**
 * Thrown to indicate that an error occurred while hashing or verifying a
 * password.
 *
 * <p>This wraps lower-level failures from the underlying hashing mechanism
 * (for example, a missing algorithm or an encoding problem) so that callers
 * can handle a single, domain-specific exception type. The originating cause
 * is preserved and accessible via {@link #getCause()}.</p>
 *
 * <p>As an unchecked exception, it does not need to be declared in a method's
 * {@code throws} clause and is usually translated into an HTTP
 * {@code 500 Internal Server Error} response by a global exception handler.</p>
 *
 */
public class PasswordHashingException extends RuntimeException {
    /**
     * Constructs a new {@code PasswordHashingException} with the specified
     * detail message and cause.
     *
     * @param message the detail message describing the hashing failure;
     *                retrievable later via {@link #getMessage()}
     * @param cause   the underlying cause of the failure (for example, the
     *                original exception thrown by the hashing library);
     *                retrievable later via {@link #getCause()}. A {@code null}
     *                value indicates that the cause is nonexistent or unknown.
     */
    public PasswordHashingException(String message, Throwable cause) {
        super(message, cause);
    }
}
