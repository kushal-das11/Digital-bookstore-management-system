package com.cts.review.exception.customexception;



/**
 * Exception thrown when a requested review is not found.
 */
public class ReviewNotFoundException extends RuntimeException {

    /**
     * Constructs a new ReviewNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining why the exception occurred
     */
    public ReviewNotFoundException(String message) {
        super(message);
    }
}
