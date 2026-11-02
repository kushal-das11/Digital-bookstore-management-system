package com.cts.catalogservice.exception;

import com.cts.catalogservice.exception.author.AuthorNotFoundException;
import com.cts.catalogservice.exception.book.BookAlreadyExistsException;
import com.cts.catalogservice.exception.book.BookDeletionException;
import com.cts.catalogservice.exception.book.BookNotFoundException;
import com.cts.catalogservice.exception.book.InvalidBookDataException;
import com.cts.catalogservice.exception.category.CategoryNotFoundException;
import com.cts.catalogservice.exception.feignclientexception.InventoryServiceDownException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * Global exception handler for handling all application-wide exceptions.
 *
 * <p>This class centralizes error handling logic and ensures consistent
 * error responses across all REST endpoints.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles BookNotFoundException.
     */
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFound(BookNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "BOOK_NOT_FOUND", ex.getMessage(), req);
    }

    /**
     * Handles BookAlreadyExistsException.
     */
    @ExceptionHandler(BookAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBookExists(BookAlreadyExistsException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "BOOK_ALREADY_EXISTS", ex.getMessage(), req);
    }


    /**
     * Handles BookDeletionException.
     */
    @ExceptionHandler(BookDeletionException.class)
    public ResponseEntity<ErrorResponse> handleBookDeletion(BookDeletionException ex, HttpServletRequest req) {
        log.error("Book deletion failed", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "BOOK_DELETION_FAILED", ex.getMessage(), req);
    }


    /**
     * Handles invalid book data.
     */
    @ExceptionHandler(InvalidBookDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBookData(InvalidBookDataException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_BOOK_DATA", ex.getMessage(), req);
    }

    /**
     * Handles AuthorNotFoundException.
     */
    @ExceptionHandler(AuthorNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuthorNotFound(AuthorNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "AUTHOR_NOT_FOUND", ex.getMessage(), req);
    }


    /**
     * Handles CategoryNotFoundException.
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFound(CategoryNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", ex.getMessage(), req);
    }


    /**
     * Handles access denial scenarios.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), req);
    }


    /**
     * Handles validation errors for request body.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message, req);
    }

    /**
     * Handles validation errors for request parameters and path variables.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest req) {

        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));

        return build(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                message,
                req
        );
    }


    /**
     * Handles Inventory Service failures.
     */
    @ExceptionHandler(InventoryServiceDownException.class)
    public ResponseEntity<ErrorResponse> handleInventoryDown(
            InventoryServiceDownException ex,
            HttpServletRequest req) {

        log.error("Inventory service failure", ex);

        return build(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Inventory Service Down",
                ex.getMessage(),
                req
        );
    }

    /**
     * Handles all unhandled exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred", req);
    }

    /**
     * Builds standardized error response.
     * @param status http status
     * @param code status code
     * @param message error message
     * @param req request for which Error response is delivered
     * @return ErrorResponse in JSON format
     */
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(code)
                .message(message)
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
