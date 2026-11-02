package com.cts.userservice.dto.response;

import com.cts.userservice.model.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response payload returned after a successful authentication.
 *
 * <p>Carries the issued access token along with enough identity context for
 * the client to use it &mdash; the user's id, role, and the token's lifetime.</p>
 *
 * <p>Lombok generates the getters, setters, no-arg and all-args constructors,
 * and a fluent builder.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    /**
     * The issued authentication token (for example, a JWT) to be sent on
     * subsequent requests.
     */
    private String token;

    /**
     * The id of the authenticated user.
     */
    private Long userId;

    /**
     * The role of the authenticated user, useful for client-side
     * authorization decisions.
     */
    private RoleName roleName;

}