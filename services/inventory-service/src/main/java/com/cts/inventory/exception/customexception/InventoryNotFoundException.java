package com.cts.inventory.exception.customexception;

/**
 * Exception thrown when inventory is not found for a given request.
 */
public class InventoryNotFoundException extends RuntimeException {

    /**
     * Constructs a new InventoryNotFoundException with the specified message.
     *
     * @param message error message describing the exception
     */
    public InventoryNotFoundException(String message) {
        super(message);
    }
}
