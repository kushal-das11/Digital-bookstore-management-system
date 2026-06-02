package com.cts.catalogservice.exception.book;

public class BookDeletionException extends RuntimeException {

    public BookDeletionException(String message) {
        super(message);
    }

    public BookDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
