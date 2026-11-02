package com.cts.inventory.exception.customexception;

/**
 * Exception thrown when inventory validation fails.
 * <p>
 * This includes cases such as invalid quantity or duplicate inventory.
 */
public class InvalidInventoryException extends RuntimeException {

    /**
     * Constructs a new InvalidInventoryException with the specified message.
     *
     * @param message error message describing the exception
     */
    public InvalidInventoryException(String message) {
        super(message);
    }
}