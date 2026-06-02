package com.cts.catalogservice.exception.feignclientexception;


/**
 * Exception thrown when the Inventory Service is unavailable.
 *
 * <p>This is typically triggered when a Feign client fails
 * to communicate with the inventory-service.</p>
 */
public class InventoryServiceDownException extends RuntimeException{

    /**
     * Constructs a new InventoryServiceDownException.
     *
     * @param message detailed error message
     */
    public InventoryServiceDownException(String message) {
        super(message);
    }
}
