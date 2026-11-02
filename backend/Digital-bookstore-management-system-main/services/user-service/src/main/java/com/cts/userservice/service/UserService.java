package com.cts.userservice.service;

import com.cts.userservice.dto.request.LoginRequest;
import com.cts.userservice.dto.request.RegisterRequest;
import com.cts.userservice.dto.request.UpdateProfileRequest;
import com.cts.userservice.dto.response.LoginResponse;
import com.cts.userservice.dto.response.UserResponse;
import com.cts.userservice.dto.response.ValidationResponse;
import java.util.List;

/**
 * Service abstraction defining the user-management and authentication
 * operations of the user service.
 *
 * <p>Declares the business operations invoked by the web layer &mdash;
 * registration, login, token validation, and profile retrieval and update
 * &mdash; while leaving persistence and security concerns to the
 * implementation. Implementations are expected to throw the application's
 * custom exceptions (for example, when a user is not found or already exists)
 * so they can be translated into HTTP responses by the global exception
 * handler.</p>
 */
public interface UserService {

    /**
     * Registers a new user and stores their hashed credentials.
     *
     * @param request the registration details (name, email, password)
     * @return the created user's public profile
     */
    UserResponse register(RegisterRequest request);

    /**
     * Authenticates a user and issues an access token.
     *
     * @param request the login credentials (email and password)
     * @return the issued token along with related identity context
     */
    LoginResponse login(LoginRequest request);

    /**
     * Retrieves a single user by their id.
     *
     * @param userId the id of the user to retrieve
     * @return the user's public profile
     */
    UserResponse getUserById(Long userId);

    /**
     * Updates an existing user's profile.
     *
     * @param userId  the id of the user whose profile is being updated
     * @param request the updated profile details (name and email)
     * @return the updated user's public profile
     */
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * Retrieves all users.
     *
     * @return the public profiles of all users
     */
    List<UserResponse> listAllUsers();

    /**
     * Validates an authentication token and resolves the associated user.
     *
     * @param authHeader the {@code Authorization} header value carrying the
     *                   token to validate
     * @return the validated user's identity
     */
    ValidationResponse validate(String authHeader);

}