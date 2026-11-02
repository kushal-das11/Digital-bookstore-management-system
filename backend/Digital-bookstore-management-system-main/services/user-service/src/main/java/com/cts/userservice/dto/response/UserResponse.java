package com.cts.userservice.dto.response;

import com.cts.userservice.model.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response payload representing a user's public profile.
 *
 * <p>Exposes only the safe, client-facing fields of a user &mdash; deliberately
 * omitting sensitive data such as the password hash &mdash; and is typically
 * returned from profile-retrieval and update endpoints.</p>
 *
 * <p>Lombok generates the getters, setters, no-arg and all-args constructors,
 * and a fluent builder.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    /**
     * The user's unique id.
     */
    private Long userId;

    /**
     * The user's display name.
     */
    private String name;

    /**
     * The user's email address.
     */
    private String email;

    /**
     * The user's assigned role.
     */
    private RoleName roleName;
}