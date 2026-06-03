package com.cts.orderservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard error response returned by GlobalExceptionHandler.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String timestamp;
    private int    status;
    private String error;
    private String message;
    private String path;
    private String traceId;
}

