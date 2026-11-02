package com.cts.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request payload for authenticating an existing user.
 *
 * <p>Bound from the body of a login request and validated before processing.
 * Both fields are mandatory; the email must additionally be a well-formed
 * address.</p>
 *
 * <p>Lombok generates the getters, setters, no-arg and all-args constructors,
 * and a fluent builder.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    /**
     * The user's email address, used as the login identifier.
     * Must be non-blank and a well-formed email.
     */
    @NotBlank
    @Email
    private String email;

    /**
     * The user's raw password, to be verified against the stored hash.
     * Must be non-blank.
     */
    @NotBlank
    private String password;
}