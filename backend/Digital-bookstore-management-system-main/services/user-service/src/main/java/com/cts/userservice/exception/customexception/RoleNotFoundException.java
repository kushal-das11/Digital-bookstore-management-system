package com.cts.userservice.exception.customexception;
/**
 * Thrown to indicate that a requested role could not be found in the system.
 *
 * <p>This is typically raised when assigning a role to a user, or when looking
 * up a role by name or identifier, and no matching role definition exists.</p>
 *
 * <p>As an unchecked exception, it does not need to be declared in a method's
 * {@code throws} clause and is usually translated into an HTTP
 * {@code 404 Not Found} response by a global exception handler.</p>
 *
 */
public class RoleNotFoundException extends RuntimeException {
    /**
     * Constructs a new {@code RoleNotFoundException} with the specified
     * detail message.
     *
     * @param message the detail message describing which role was not found;
     *                retrievable later via {@link #getMessage()}
     */
    public RoleNotFoundException(String message) {
        super(message);
    }
}
