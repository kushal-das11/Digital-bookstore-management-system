package com.cts.review.exception.customexception;

public class ReviewOperationException extends RuntimeException {
    public ReviewOperationException(String message) {
        super(message);
    }
}
