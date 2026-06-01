package com.cts.review.exception;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * Represents a standardized error response returned by the API.
*/
@Data
@AllArgsConstructor
public class ErrorResponse {

    /**
     * The timestamp indicating when the error occurred.
     */
    @NotNull(message = "Timestamp cannot be null")
    private LocalDateTime timestamp;


    /**
     * HTTP status code associated with the error.
     */
    @NotNull(message = "Status code is required")
    private int status;


    /**
     * Short description of the error (e.g., BAD_REQUEST, NOT_FOUND).
     */
    @NotBlank(message = "Error description cannot be blank")
    private String error;


    /**
     * Detailed message explaining the error.
     */
    @NotBlank(message = "Error message cannot be blank")
    private String message;
}
