package com.cts.orderservice.exception;

import com.cts.orderservice.exception.feignclientexception.CatalogServiceDownException;
import com.cts.orderservice.exception.feignclientexception.InventoryServiceDownException;
import com.cts.orderservice.exception.inventory.InsufficientStockException;
import com.cts.orderservice.exception.order.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 * Catches all exceptions and returns structured ErrorResponse.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---- ORDER EXCEPTIONS ----

    /**
     * Handles OrderNotFoundException and returns 404 NOT FOUND.
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(
            OrderNotFoundException ex,
            HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND,
                "ORDER_NOT_FOUND",
                ex.getMessage(), req);
    }

    /**
     * Handles CartItemNotFoundException and returns 404 NOT FOUND.
     */
    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCartNotFound(
            CartItemNotFoundException ex,
            HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND,
                "CART_ITEM_NOT_FOUND",
                ex.getMessage(), req);
    }

    /**
     * Handles InvalidOrderException and returns 400 BAD REQUEST.
     */
    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrder(
            InvalidOrderException ex,
            HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "INVALID_ORDER",
                ex.getMessage(), req);
    }

    /**
     * Handles InvalidOrderStatusException and returns 400 BAD REQUEST.
     */
    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(
            InvalidOrderStatusException ex,
            HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "INVALID_STATUS",
                ex.getMessage(), req);
    }

    // ---- INVENTORY EXCEPTIONS ----

    /**
     * Handles InsufficientStockException and returns 409 CONFLICT.
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleStock(
            InsufficientStockException ex,
            HttpServletRequest req) {
        return build(HttpStatus.CONFLICT,
                "INSUFFICIENT_STOCK",
                ex.getMessage(), req);
    }

    // ---- SERVICE DOWN EXCEPTIONS ----

    /**
     * Handles CatalogServiceDownException and returns 503 SERVICE UNAVAILABLE.
     */
    @ExceptionHandler(CatalogServiceDownException.class)
    public ResponseEntity<ErrorResponse> handleCatalogDown(
            CatalogServiceDownException ex,
            HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE,
                "CATALOG_SERVICE_DOWN",
                ex.getMessage(), req);
    }

    /**
     * Handles InventoryServiceDownException and returns 503 SERVICE UNAVAILABLE.
     */
    @ExceptionHandler(InventoryServiceDownException.class)
    public ResponseEntity<ErrorResponse> handleInventoryDown(
            InventoryServiceDownException ex,
            HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE,
                "INVENTORY_SERVICE_DOWN",
                ex.getMessage(), req);
    }

    // ---- VALIDATION EXCEPTION ----

    /**
     * Handles @Valid constraint violations and returns 400 BAD REQUEST.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest req) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField()
                        + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR", message, req);
    }

    // ---- GENERAL EXCEPTIONS ----

    /**
     * Handles all uncaught exceptions and returns 500 INTERNAL SERVER ERROR.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex,
            HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                ex.getMessage(), req);
    }

    // ---- BUILDER ----

    /**
     * Builds a structured ErrorResponse with timestamp, status, and trace ID.
     */
    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String error,
            String message, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                error,
                message,
                req.getRequestURI(),
                UUID.randomUUID().toString()
                        .substring(0, 8)
        );
        return new ResponseEntity<>(body, status);
    }
}
