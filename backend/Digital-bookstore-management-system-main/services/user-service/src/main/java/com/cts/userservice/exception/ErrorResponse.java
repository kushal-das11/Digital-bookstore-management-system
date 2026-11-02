package com.cts.userservice.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
/**
 * Immutable data transfer object representing a standardized error payload
 * returned to API clients when a request fails.
 *
 * <p>Instances are produced centrally by the {@link GlobalExceptionHandler}
 * and serialized to JSON as the body of an error response, giving callers a
 * consistent structure regardless of which exception was raised.</p>
 *
 * <p>The class is built with Lombok: {@code @Getter} generates accessors,
 * {@code @Builder} provides the fluent builder used by the handler, and
 * {@code @AllArgsConstructor} generates the constructor the builder relies on.
 * All fields are {@code final}, making instances effectively immutable.</p>
 *
 * <p>Example JSON output:</p>
 * <pre>{@code
 * {
 *   "timestamp": "2025-06-01T10:15:30Z",
 *   "status": 404,
 *   "error": "USER_NOT_FOUND",
 *   "message": "No user found with id 42",
 *   "path": "/api/users/42"
 * }
 * }</pre>
 *
 */
@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {
    /**
     * The instant at which the error response was generated.
     *
     * <p>Serialized as an ISO-8601 string (rather than a numeric epoch value)
     * because of the {@link JsonFormat} annotation.</p>
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant timestamp;
    /**
     * The numeric HTTP status code associated with the error
     * (for example, {@code 404} or {@code 500}).
     */
    private final int status;
    /**
     * A short, machine-readable error code identifying the error category
     * (for example, {@code USER_NOT_FOUND} or {@code VALIDATION_FAILED}).
     */
    private final String error;
    /**
     * A human-readable description of what went wrong, suitable for logging
     * or display to a developer consuming the API.
     */
    private final String message;
    /**
     * The request URI that triggered the error, useful for correlating the
     * response with the originating endpoint.
     */
    private final String path;
}
