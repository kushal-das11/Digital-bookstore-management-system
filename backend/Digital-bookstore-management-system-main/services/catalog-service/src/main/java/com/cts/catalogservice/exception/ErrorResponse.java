package com.cts.catalogservice.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


/**
 * Standard error response structure for API exceptions.
 *
 * <p>This class is used to send consistent error details
 * back to clients when an exception occurs.</p>
 *
 * <p>Includes timestamp, status code, error type, message,
 * and request path.</p>
 */
@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Time at which the error occurred.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant timestamp;

    /**
     * HTTP status code.
     */
    private final int status;

    /**
     * Application-specific error code.
     */
    private final String error;

    /**
     * Human-readable error message.
     */
    private final String message;

    /**
     * API endpoint path where the error occurred.
     */
    private final String path;
}
