package com.cts.inventory.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents the standard error response returned by the API.
 */
@Data
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Timestamp when the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code of the error.
     */
    private int status;

    /**
     * Error type or reason phrase.
     */
    private String error;

    /**
     * Detailed error message.
     */
    private String message;
}