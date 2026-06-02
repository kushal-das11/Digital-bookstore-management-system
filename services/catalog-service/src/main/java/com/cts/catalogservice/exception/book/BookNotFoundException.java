package com.cts.catalogservice.exception.book;


/**
 * Exception thrown when a requested book is not found.
 *
 * <p>This is typically raised when querying a book
 * by ID that does not exist in the catalog.</p>
 */
public class BookNotFoundException extends RuntimeException {

    /**
     * Constructs a new BookNotFoundException.
     *
     * @param message detailed error message
     */
    public BookNotFoundException(String message) {
        super(message);
    }
}
