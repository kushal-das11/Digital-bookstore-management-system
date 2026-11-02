package com.cts.review.exception.customexception;

/**
 * Exception thrown when a review is invalid.
*/
public class InvalidReviewException extends RuntimeException {

    /**
     * Constructs a new InvalidReviewException with a specific message.
     *
     * @param message detailed error message explaining the reason for the exception
     */
    public InvalidReviewException(String message) {
        super(message);
    }
}
