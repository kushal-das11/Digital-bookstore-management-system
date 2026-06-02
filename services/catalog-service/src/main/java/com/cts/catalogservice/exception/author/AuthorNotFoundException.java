package com.cts.catalogservice.exception.author;


/**
 * Exception thrown when an author is not found in the system.
 *
 * <p>This exception is typically raised when attempting to
 * retrieve or reference an author using an invalid or non-existing ID.</p>
 */
public class AuthorNotFoundException extends RuntimeException {

    /**
     * Constructs a new AuthorNotFoundException.
     *
     * @param message detailed error message
     */
    public AuthorNotFoundException(String message) {
        super(message);
    }
}
