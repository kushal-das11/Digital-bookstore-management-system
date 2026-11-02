package com.cts.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request payload for updating an existing user's profile.
 *
 * <p>Bound from the body of a profile-update request and validated before the
 * changes are applied. Carries the editable profile fields (name and email);
 * both are mandatory, and the email must be a well-formed address.</p>
 *
 * <p>Lombok generates the getters, setters, no-arg and all-args constructors,
 * and a fluent builder.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    /**
     * The user's updated display name. Must be non-blank and at most 100
     * characters.
     */
    @NotBlank
    @Size(max = 100)
    private String name;

    /**
     * The user's updated email address. Must be non-blank, a well-formed
     * email, and at most 150 characters.
     */
    @NotBlank
    @Email
    @Size(max = 150)
    private String email;
}