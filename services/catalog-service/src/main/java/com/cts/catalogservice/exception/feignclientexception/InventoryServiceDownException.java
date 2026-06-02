package com.cts.catalogservice.exception.feignclientexception;

public class InventoryServiceDownException extends RuntimeException{
    public InventoryServiceDownException(String message) {
        super(message);
    }
}
