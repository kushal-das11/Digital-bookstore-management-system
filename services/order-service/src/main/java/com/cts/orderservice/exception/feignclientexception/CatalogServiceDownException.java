package com.cts.orderservice.exception.feignclientexception;

public class CatalogServiceDownException extends RuntimeException{
    public CatalogServiceDownException(String message) {
        super(message);
    }
}
