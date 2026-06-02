package com.cts.userservice.controller;

import com.cts.userservice.dto.request.LoginRequest;
import com.cts.userservice.dto.request.RegisterRequest;
import com.cts.userservice.dto.request.UpdateProfileRequest;
import com.cts.userservice.dto.response.LoginResponse;
import com.cts.userservice.dto.response.UserResponse;
import com.cts.userservice.dto.response.ValidationResponse;
import com.cts.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing user-management and authentication endpoints under
 * the {@code /api/users} base path.
 *
 * <p>Acts as a thin web layer: it binds and validates incoming requests, then
 * delegates all business logic to {@link UserService} and wraps the results in
 * {@link ResponseEntity} objects with appropriate HTTP status codes. Validation
 * failures and domain exceptions are translated into error responses by the
 * global exception handler rather than being handled here.</p>
 *
 * <p>Lombok's {@code @RequiredArgsConstructor} generates the constructor used
 * to inject the {@code final} {@link UserService} dependency.</p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    /** Service handling all user-related business logic. */
    private final UserService userService;

    /**
     * Registers a new user.
     *
     * <p>Handles {@code POST /api/users/register}. The request body is
     * validated before processing.</p>
     *
     * @param request the registration details (name, email, password)
     * @return a {@code 201 CREATED} response containing the created user's
     *         public profile
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(request));
    }

    /**
     * Authenticates a user and issues an access token.
     *
     * <p>Handles {@code POST /api/users/login}. The request body is validated
     * before processing.</p>
     *
     * @param request the login credentials (email and password)
     * @return a {@code 200 OK} response containing the issued token and
     *         related identity context
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    /**
     * Validates an authentication token.
     *
     * <p>Handles {@code POST /api/users/auth/validate}. Intended for use by the
     * API gateway to verify a token before forwarding a request, returning the
     * resolved user identity on success.</p>
     *
     * @param authHeader the value of the {@code Authorization} request header,
     *                   carrying the token to validate
     * @return a {@code 200 OK} response containing the validated user's id
     */
    @PostMapping("/auth/validate")
    public ResponseEntity<ValidationResponse> validate(
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(userService.validate(authHeader));
    }

    /**
     * Retrieves all users.
     *
     * <p>Handles {@code GET /api/users}.</p>
     *
     * @return a {@code 200 OK} response containing the list of all users'
     *         public profiles
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> listAll() {
        return ResponseEntity.ok(userService.listAllUsers());
    }

    /**
     * Retrieves a single user by id.
     *
     * <p>Handles {@code GET /api/users/{userId}}.</p>
     *
     * @param userId the id of the user to retrieve
     * @return a {@code 200 OK} response containing the user's public profile
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    /**
     * Updates an existing user's profile.
     *
     * <p>Handles {@code PUT /api/users/{userId}}. The request body is validated
     * before the changes are applied.</p>
     *
     * @param userId  the id of the user whose profile is being updated
     * @param request the updated profile details (name and email)
     * @return a {@code 200 OK} response containing the updated user profile
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }
}