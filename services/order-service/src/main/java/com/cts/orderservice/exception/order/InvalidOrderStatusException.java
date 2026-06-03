package com.cts.orderservice.exception.order;
public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(String message) { super(message); }
}
