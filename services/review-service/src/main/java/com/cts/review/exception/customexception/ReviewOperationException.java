package com.cts.review.exception.customexception;


/**
 * Exception thrown when an error occurs during a review operation.
*/
public class ReviewOperationException extends RuntimeException {

    /**
     * Constructs a new ReviewOperationException with the specified detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public ReviewOperationException(String message) {
        super(message);
    }
}
