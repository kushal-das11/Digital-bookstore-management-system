package com.cts.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
/**
 * Lightweight response payload returned when validating a token or credentials.
 *
 * <p>Conveys only the id of the validated user, allowing a caller (for
 * example, another service verifying a token) to resolve the principal
 * without exposing any additional profile data.</p>
 *
 * <p>Lombok's {@code @Data} generates getters, setters, {@code equals},
 * {@code hashCode}, and {@code toString}; {@code @AllArgsConstructor} and
 * {@code @Builder} provide an all-args constructor and a fluent builder.</p>
 */
@Data
@AllArgsConstructor
@Builder
public class ValidationResponse {
    /**
     * The id of the validated user.
     */
    private Long userId;
}
