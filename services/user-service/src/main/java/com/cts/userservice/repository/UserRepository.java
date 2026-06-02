package com.cts.userservice.repository;

import com.cts.userservice.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * Spring Data JPA repository for {@link User} entities.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD and pagination
 * operations for {@code User}, whose primary key is of type {@link Long}.
 * The additional methods declared below are derived queries: Spring Data
 * generates their implementations automatically from the method names.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the matching user, or an empty
     *         {@code Optional} if no user has the given email
     */
    Optional<User> findByEmail(String email);
    /**
     * Checks whether a user with the given email address exists, without
     * loading the entity.
     *
     * @param email the email address to check
     * @return {@code true} if a user with the given email exists,
     *         {@code false} otherwise
     */
    boolean existsByEmail(String email);
}
