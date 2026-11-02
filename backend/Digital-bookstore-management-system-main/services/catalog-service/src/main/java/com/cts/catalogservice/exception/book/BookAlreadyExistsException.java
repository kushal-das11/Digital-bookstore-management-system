package com.cts.catalogservice.exception.book;


/**
 * Exception thrown when attempting to create a book
 * that already exists in the system.
 */
public class BookAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new BookAlreadyExistsException.
     *
     * @param message detailed error message
     */
    public BookAlreadyExistsException(String message) {
        super(message);
    }
}
