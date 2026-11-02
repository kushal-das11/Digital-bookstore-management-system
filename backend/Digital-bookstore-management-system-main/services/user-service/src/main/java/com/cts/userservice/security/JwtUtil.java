package com.cts.userservice.security;

import com.cts.userservice.model.RoleName;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

/**
 * Utility component for creating and parsing JSON Web Tokens (JWTs).
 *
 * <p>Handles token generation and the extraction and verification of claims
 * using an HMAC-SHA signing key. The signing secret and token lifetime are
 * supplied from the {@code jwt.secret} and {@code jwt.expiration} configuration
 * properties; the secret is converted into a {@link Key} once during
 * initialization.</p>
 *
 * <p>Registered as a Spring {@link Component} so it can be injected wherever
 * token handling is required (for example, the service layer and the
 * authentication filter).</p>
 */
@Component
public class JwtUtil {

    /** Raw signing secret, injected from the {@code jwt.secret} property. */
    @Value("${jwt.secret}")
    private String secret;

    /** Token time-to-live in milliseconds, from the {@code jwt.expiration} property. */
    @Value("${jwt.expiration}")
    private long expirationMs;

    /** The HMAC signing key derived from {@link #secret} during initialization. */
    private Key secretKey;

    /**
     * Initializes the signing key from the configured secret.
     *
     * <p>Invoked automatically after dependency injection via
     * {@link PostConstruct}, building an HMAC-SHA key from the secret's bytes.</p>
     */
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }


    /**
     * Generates a signed JWT for the given user and role.
     *
     * <p>The user id is stored as the token subject, the role as a custom
     * {@code role} claim, with issued-at and expiration timestamps based on the
     * configured lifetime.</p>
     *
     * @param userId the id of the user the token is issued for
     * @param role   the user's role, stored as a claim
     * @return the compact, signed JWT string
     */
    public String generateToken(Long userId, RoleName role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Parses and verifies a token, returning its claims.
     *
     * @param token the compact JWT string to parse
     * @return the token's claims payload
     * @throws io.jsonwebtoken.JwtException if the token is invalid, expired, or
     *                                      has an invalid signature
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the user id (token subject) from a token.
     *
     * @param token the compact JWT string
     * @return the user id stored as the token's subject
     */
    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extracts the role claim from a token.
     *
     * @param token the compact JWT string
     * @return the value of the {@code role} claim
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * Checks whether a token is valid (well-formed, correctly signed, and not
     * expired).
     *
     * @param token the compact JWT string to validate
     * @return {@code true} if the token parses and verifies successfully,
     *         {@code false} if it is malformed, tampered with, or expired
     */
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}