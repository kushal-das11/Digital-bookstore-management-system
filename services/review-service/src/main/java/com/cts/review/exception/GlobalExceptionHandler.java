package com.cts.review.exception;

import com.cts.review.exception.customexception.InvalidReviewException;
import com.cts.review.exception.customexception.ReviewNotFoundException;
import com.cts.review.exception.customexception.ReviewOperationException;
import com.cts.review.exception.feignclientexception.CatalogServiceDownException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Not Found
    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ReviewNotFoundException ex) {
        return new ResponseEntity<>(
                buildError(ex.getMessage(), HttpStatus.NOT_FOUND),
                HttpStatus.NOT_FOUND
        );
    }

    // Invalid Input
    @ExceptionHandler(InvalidReviewException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(InvalidReviewException ex) {
        return new ResponseEntity<>(
                buildError(ex.getMessage(), HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST
        );
    }

    // Business Failure
    @ExceptionHandler(ReviewOperationException.class)
    public ResponseEntity<ErrorResponse> handleOperation(ReviewOperationException ex) {
        return new ResponseEntity<>(
                buildError(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(CatalogServiceDownException.class)
    public ResponseEntity<ErrorResponse> handleCatalogDown(
            CatalogServiceDownException ex,
            HttpServletRequest req) {

        log.error("Catalog service failure", ex);

        return new ResponseEntity<>(buildError(
                ex.getMessage(),
                HttpStatus.SERVICE_UNAVAILABLE), HttpStatus.SERVICE_UNAVAILABLE);
    }

    // Generic fallback (VERY IMPORTANT)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return new ResponseEntity<>(
                buildError("Unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // Reusable builder
    private ErrorResponse buildError(String message, HttpStatus status) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );
    }
}
