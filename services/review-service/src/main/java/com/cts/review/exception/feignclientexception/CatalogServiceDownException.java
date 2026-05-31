package com.cts.review.exception.feignclientexception;

public class CatalogServiceDownException extends RuntimeException{
    public CatalogServiceDownException(String message) {
        super(message);
    }
}
