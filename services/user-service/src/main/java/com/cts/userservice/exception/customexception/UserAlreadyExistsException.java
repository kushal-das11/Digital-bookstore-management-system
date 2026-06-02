package com.cts.userservice.exception.customexception;
/**
 * Thrown to indicate that an attempt was made to create or register a user
 * who already exists in the system.
 *
 * <p>This typically occurs during user registration when the supplied
 * unique identifier (for example, a username or email address) is already
 * associated with an existing account.</p>
 *
 * <p>As an unchecked exception, it does not need to be declared in a method's
 * {@code throws} clause and is usually translated into an HTTP
 * {@code 409 Conflict} response by a global exception handler.</p>
 *
 */
public class UserAlreadyExistsException extends RuntimeException {
    /**
     * Constructs a new {@code UserAlreadyExistsException} with the specified
     * detail message.
     *
     * @param message the detail message describing which user already exists;
     *                retrievable later via {@link #getMessage()}
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
