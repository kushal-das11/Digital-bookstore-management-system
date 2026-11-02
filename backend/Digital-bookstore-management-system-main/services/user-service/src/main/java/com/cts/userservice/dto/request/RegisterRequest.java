package com.cts.userservice.dto.request;

import com.cts.userservice.model.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request payload for registering a new user account.
 *
 * <p>Bound from the body of a registration request and validated before a
 * user is created. All fields are mandatory and subject to the size
 * constraints below; the email must also be a well-formed address.</p>
 *
 * <p>Lombok generates the getters, setters, no-arg and all-args constructors,
 * and a fluent builder.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    /**
     * The new user's display name. Must be non-blank and at most 100
     * characters.
     */
    @NotBlank
    @Size(max = 100)
    private String name;

    /**
     * The new user's email address, used as the unique login identifier.
     * Must be non-blank, a well-formed email, and at most 150 characters.
     */
    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    /**
     * The new user's raw password, which is hashed before storage.
     * Must be non-blank and between 8 and 100 characters.
     */
    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

}