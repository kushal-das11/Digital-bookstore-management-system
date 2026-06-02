package com.cts.userservice.tests;


import com.cts.userservice.model.RoleName;
import com.cts.userservice.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JwtUtil}. These use a real HMAC key (no mocking) so the
 * full sign/verify round trip is exercised.
 */
class JwtUtilTest {

    // HMAC-SHA256 requires a key of at least 256 bits (32 bytes).
    private static final String SECRET = "test-secret-key-that-is-long-enough-1234567890";
    private static final long EXPIRATION_MS = 3_600_000L;

    private JwtUtil jwtUtil;

    /**
     * Builds a {@link JwtUtil} with a test secret and expiration, then invokes
     * {@code init()} to derive the signing key (normally done via
     * {@code @PostConstruct}).
     */
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", EXPIRATION_MS);
        jwtUtil.init();
    }

    /**
     * Verifies a generated token round-trips both the user id (subject) and the
     * role claim.
     */
    @Test
    @DisplayName("generated token round-trips user id and role")
    void generateAndExtract() {
        String token = jwtUtil.generateToken(42L, RoleName.ADMIN);

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractUserId(token)).isEqualTo("42");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("ADMIN");
    }

    /**
     * Verifies the parsed claims expose the subject, role, and an expiration
     * that follows the issued-at time.
     */
    @Test
    @DisplayName("claims expose subject, role, and an expiry after issuance")
    void claimsContainExpectedValues() {
        String token = jwtUtil.generateToken(7L, RoleName.CUSTOMER);
        Claims claims = jwtUtil.extractClaims(token);

        assertThat(claims.getSubject()).isEqualTo("7");
        assertThat(claims.get("role", String.class)).isEqualTo("CUSTOMER");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    /** Verifies a freshly issued, correctly signed token is reported valid. */
    @Test
    @DisplayName("isTokenValid returns true for a freshly issued token")
    void isTokenValid_true() {
        String token = jwtUtil.generateToken(1L, RoleName.CUSTOMER);
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    /** Verifies a structurally malformed token is reported invalid. */
    @Test
    @DisplayName("isTokenValid returns false for a malformed token")
    void isTokenValid_malformed() {
        assertThat(jwtUtil.isTokenValid("not.a.jwt")).isFalse();
    }

    /**
     * Verifies a token signed with a different secret fails signature
     * verification and is reported invalid.
     */
    @Test
    @DisplayName("isTokenValid returns false for a token signed with a different key")
    void isTokenValid_wrongSignature() {
        JwtUtil other = new JwtUtil();
        ReflectionTestUtils.setField(other, "secret", "a-totally-different-secret-key-9876543210ABCDEF");
        ReflectionTestUtils.setField(other, "expirationMs", EXPIRATION_MS);
        other.init();

        String foreignToken = other.generateToken(1L, RoleName.ADMIN);

        assertThat(jwtUtil.isTokenValid(foreignToken)).isFalse();
    }

    /**
     * Verifies a token whose expiry is in the past is reported invalid. The
     * token is minted by a separate instance configured with a negative
     * lifetime so it is already expired on creation.
     */
    @Test
    @DisplayName("isTokenValid returns false for an expired token")
    void isTokenValid_expired() {
        JwtUtil shortLived = new JwtUtil();
        ReflectionTestUtils.setField(shortLived, "secret", SECRET);
        ReflectionTestUtils.setField(shortLived, "expirationMs", -1_000L);
        shortLived.init();

        String expired = shortLived.generateToken(1L, RoleName.CUSTOMER);

        assertThat(jwtUtil.isTokenValid(expired)).isFalse();
    }
}
