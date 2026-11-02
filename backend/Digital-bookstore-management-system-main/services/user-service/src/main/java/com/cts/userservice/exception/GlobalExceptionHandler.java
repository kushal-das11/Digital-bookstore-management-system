package com.cts.userservice.exception;

import com.cts.userservice.exception.customexception.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handler for the user service's REST layer.
 *
 * <p>Annotated with {@link RestControllerAdvice}, this class intercepts
 * exceptions thrown by any controller in the application and converts them
 * into a consistent {@link ErrorResponse} payload with an appropriate HTTP
 * status code. This keeps controllers and services free of repetitive
 * try/catch and response-building logic.</p>
 *
 * <p>Each handler maps a specific exception type to an HTTP status and a
 * machine-readable error code:</p>
 * <ul>
 *   <li>{@link UserNotFoundException}, {@link RoleNotFoundException}
 *       &rarr; {@code 404 NOT_FOUND}</li>
 *   <li>{@link UserAlreadyExistsException} &rarr; {@code 409 CONFLICT}</li>
 *   <li>{@link InvalidPasswordException} &rarr; {@code 401 UNAUTHORIZED}</li>
 *   <li>{@link AccessDeniedException} &rarr; {@code 403 FORBIDDEN}</li>
 *   <li>{@link PasswordHashingException} &rarr; {@code 500 INTERNAL_SERVER_ERROR}</li>
 *   <li>{@link MethodArgumentNotValidException} &rarr; {@code 400 BAD_REQUEST}</li>
 *   <li>Any other {@link Exception} &rarr; {@code 500 INTERNAL_SERVER_ERROR}</li>
 * </ul>
 *
 * <p>Server-side failures (hashing errors and unhandled exceptions) are logged
 * at error level; client-caused failures are not logged, to avoid noise.</p>
 *
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles the case where a requested user does not exist.
     *
     * @param ex  the thrown exception carrying the detail message
     * @param req the current request, used to record the offending URI
     * @return a {@code 404 NOT_FOUND} response with error code {@code USER_NOT_FOUND}
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex.getMessage(), req);
    }

    /**
     * Handles the case where a requested role does not exist.
     *
     * @param ex  the thrown exception carrying the detail message
     * @param req the current request, used to record the offending URI
     * @return a {@code 404 NOT_FOUND} response with error code {@code ROLE_NOT_FOUND}
     */
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotFound(RoleNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "ROLE_NOT_FOUND", ex.getMessage(), req);
    }

    /**
     * Handles attempts to create a user that already exists.
     *
     * @param ex  the thrown exception carrying the detail message
     * @param req the current request, used to record the offending URI
     * @return a {@code 409 CONFLICT} response with error code {@code USER_ALREADY_EXISTS}
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserExists(UserAlreadyExistsException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", ex.getMessage(), req);
    }

    /**
     * Handles password verification or validation failures.
     *
     * @param ex  the thrown exception carrying the detail message
     * @param req the current request, used to record the offending URI
     * @return a {@code 401 UNAUTHORIZED} response with error code {@code INVALID_PASSWORD}
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(InvalidPasswordException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", ex.getMessage(), req);
    }

    /**
     * Handles authorization failures where the caller lacks the required
     * permissions.
     *
     * @param ex  the thrown exception carrying the detail message
     * @param req the current request, used to record the offending URI
     * @return a {@code 403 FORBIDDEN} response with error code {@code ACCESS_DENIED}
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), req);
    }

    /**
     * Handles failures in the password hashing subsystem.
     *
     * <p>This represents a server-side fault, so the exception is logged at
     * error level (including its stack trace) before the response is built.</p>
     *
     * @param ex  the thrown exception carrying the detail message and cause
     * @param req the current request, used to record the offending URI
     * @return a {@code 500 INTERNAL_SERVER_ERROR} response with error code
     * {@code PASSWORD_HASHING_FAILED}
     */
    @ExceptionHandler(PasswordHashingException.class)
    public ResponseEntity<ErrorResponse> handlePasswordHashing(PasswordHashingException ex, HttpServletRequest req) {
        log.error("Password hashing failed", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "PASSWORD_HASHING_FAILED", ex.getMessage(), req);
    }

    /**
     * Handles bean-validation failures on request bodies annotated with
     * {@code @Valid}.
     *
     * <p>Each field error is flattened into a {@code field: message} entry,
     * and all entries are joined with {@code "; "} to form a single,
     * human-readable summary of every validation problem.</p>
     *
     * @param ex  the validation exception containing the binding result
     * @param req the current request, used to record the offending URI
     * @return a {@code 400 BAD_REQUEST} response with error code {@code VALIDATION_FAILED}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message, req);
    }

    /**
     * Fallback handler for any exception not matched by a more specific
     * handler above.
     *
     * <p>The exception is logged at error level, and a generic message is
     * returned to the client to avoid leaking internal implementation
     * details.</p>
     *
     * @param ex  the unhandled exception
     * @param req the current request, used to record the offending URI
     * @return a {@code 500 INTERNAL_SERVER_ERROR} response with error code {@code INTERNAL_ERROR}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred", req);
    }

    /**
     * Builds a standardized {@link ResponseEntity} wrapping an
     * {@link ErrorResponse}.
     *
     * <p>Shared by all handler methods to ensure every error response has the
     * same shape: a current timestamp, the numeric status, an error code, a
     * message, and the request path.</p>
     *
     * @param status  the HTTP status to apply to both the response and the body
     * @param code    the machine-readable error code
     * @param message the human-readable error description
     * @param req     the current request, used to extract the request URI
     * @return a fully populated response entity carrying the error body
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
