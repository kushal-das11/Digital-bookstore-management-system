package com.cts.userservice.model;
/**
 * Enumeration of the roles a user can hold within the system.
 *
 * <p>Persisted as a string (via {@code @Enumerated(EnumType.STRING)} on the
 * {@link Role} entity) so that the readable name &mdash; rather than the
 * ordinal &mdash; is stored in the database. This keeps the data resilient to
 * reordering of the constants.</p>
 *
 */
public enum RoleName {
    /** A standard end user with customer-level privileges. */
    CUSTOMER,
    /** A user with elevated administrative privileges. */
    ADMIN
}
