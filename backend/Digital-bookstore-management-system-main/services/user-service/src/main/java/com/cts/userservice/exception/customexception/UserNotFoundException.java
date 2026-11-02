package com.cts.userservice.exception.customexception;
/**
 * Thrown to indicate that a requested user could not be found in the system.
 *
 * <p>This is typically raised when a lookup by identifier (such as a user ID,
 * username, or email address) yields no matching record &mdash; for example
 * during authentication, profile retrieval, or update operations.</p>
 *
 * <p>As an unchecked exception, it does not need to be declared in a method's
 * {@code throws} clause and is usually translated into an HTTP
 * {@code 404 Not Found} response by a global exception handler.</p>
 *
 */
public class UserNotFoundException extends RuntimeException {
    /**
     * Constructs a new {@code UserNotFoundException} with the specified
     * detail message.
     *
     * @param message the detail message describing which user was not found;
     *                retrievable later via {@link #getMessage()}
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
