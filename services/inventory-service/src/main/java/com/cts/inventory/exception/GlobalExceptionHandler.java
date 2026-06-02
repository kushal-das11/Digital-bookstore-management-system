package com.cts.inventory.exception;

import com.cts.inventory.exception.customexception.InvalidInventoryException;
import com.cts.inventory.exception.customexception.InventoryNotFoundException;
import com.cts.inventory.exception.customexception.OutOfStockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;


/**
 * Handles global exceptions across the application
 * and returns standardized error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * Handles InventoryNotFoundException.
     *
     * @param ex exception details
     * @return error response with NOT_FOUND status
     */
    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(InventoryNotFoundException ex) {
        return new ResponseEntity<>(
                buildError(ex.getMessage(), HttpStatus.NOT_FOUND),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Handles InvalidInventoryException.
     *
     * @param ex exception details
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(InvalidInventoryException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(InvalidInventoryException ex) {
        return new ResponseEntity<>(
                buildError(ex.getMessage(), HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handles OutOfStockException.
     *
     * @param ex exception details
     * @return error response with CONFLICT status
     */
    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ErrorResponse> handleStock(OutOfStockException ex) {
        return new ResponseEntity<>(
                buildError(ex.getMessage(), HttpStatus.CONFLICT),
                HttpStatus.CONFLICT
        );
    }

    /**
     * Handles Missing Request header X-User-Id .
     *
     * @param ex exception details
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        return new ResponseEntity<>(
                buildError(ex.getMessage(), HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handles Invalid Request .
     *
     * @param ex exception details
     * @return error response with BAD_REQUEST(404) status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        return new ResponseEntity<>(
                buildError(message, HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST
        );
    }


    /**
     * Builds a sta_ndard error response.
     *
     * @param message error message
     * @param status HTTP status
     * @return formatted error response
     */
    private ErrorResponse buildError(String message, HttpStatus status) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );
    }
}
