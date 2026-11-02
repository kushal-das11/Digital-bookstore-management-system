package com.cts.userservice.exception.customexception;
/**
 * Thrown to indicate that a supplied password is invalid.
 *
 * <p>This may occur when a password fails verification during login, or when
 * a new or changed password does not satisfy the system's validation rules
 * (for example, minimum length or required character classes).</p>
 *
 * <p>As an unchecked exception, it does not need to be declared in a method's
 * {@code throws} clause and is usually translated into an HTTP
 * {@code 400 Bad Request} or {@code 401 Unauthorized} response by a global
 * exception handler, depending on the context.</p>
 *
 */
public class InvalidPasswordException extends RuntimeException {
    /**
     * Constructs a new {@code InvalidPasswordException} with the specified
     * detail message.
     *
     * @param message the detail message describing why the password is invalid;
     *                retrievable later via {@link #getMessage()}
     */
    public InvalidPasswordException(String message) {
        super(message);
    }
}
