package com.cts.catalogservice.exception.book;

public class InvalidBookDataException extends RuntimeException {

    public InvalidBookDataException(String message) {
        super(message);
    }
}
