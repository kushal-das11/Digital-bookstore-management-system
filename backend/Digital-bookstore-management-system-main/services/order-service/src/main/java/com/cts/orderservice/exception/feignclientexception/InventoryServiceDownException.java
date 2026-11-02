package com.cts.orderservice.exception.feignclientexception;

public class InventoryServiceDownException extends RuntimeException{
    public InventoryServiceDownException(String message) {
        super(message);
    }
}
