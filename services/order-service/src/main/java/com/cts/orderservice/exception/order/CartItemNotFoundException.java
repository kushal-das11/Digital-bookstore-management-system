package com.cts.orderservice.exception.order;
public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(String message) { super(message); }
}

