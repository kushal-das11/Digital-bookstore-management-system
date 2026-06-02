package com.cts.catalogservice.exception.book;


/**
 * Exception thrown when a book deletion operation fails.
 *
 * <p>This may occur due to database constraints, dependencies,
 * or unexpected errors during deletion.</p>
 */
public class BookDeletionException extends RuntimeException {

    /**
     * Constructs a new BookDeletionException with message.
     *
     * @param message detailed error message
     */
    public BookDeletionException(String message) {
        super(message);
    }


    /**
     * Constructs a new BookDeletionException with message and cause.
     *
     * @param message detailed error message
     * @param cause   root cause of the exception
     */
    public BookDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
