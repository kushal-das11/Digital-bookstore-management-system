package com.cts.review.exception.feignclientexception;


/**
 * Exception thrown when the Catalog Service is unavailable or not responding.
*/
public class CatalogServiceDownException extends RuntimeException{

    /**
     * Constructs a new CatalogServiceDownException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the failure
     */
    public CatalogServiceDownException(String message) {
        super(message);
    }
}
