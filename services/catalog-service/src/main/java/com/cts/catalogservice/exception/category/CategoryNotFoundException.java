package com.cts.catalogservice.exception.category;

/**
 * Exception thrown when a category is not found.
 */
public class CategoryNotFoundException extends RuntimeException {

    /**
     * Constructs a new CategoryNotFoundException.
     *
     * @param message detailed error message
     *
     */
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
