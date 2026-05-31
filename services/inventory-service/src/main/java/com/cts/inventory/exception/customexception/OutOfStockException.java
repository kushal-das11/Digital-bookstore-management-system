package com.cts.inventory.exception.customexception;

/**
 * Exception thrown when requested quantity exceeds available stock.
 */
public class OutOfStockException extends RuntimeException {

    /**
     * Constructs a new OutOfStockException with the specified message.
     *
     * @param message error message describing the exception
     */
    public OutOfStockException(String message) {
        super(message);
    }
}