package com.cts.userservice.repository;

import com.cts.userservice.model.Authentication;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * Spring Data JPA repository for {@link Authentication} entities.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD and pagination
 * operations for {@code Authentication}, whose primary key is of type
 * {@link Integer}. The additional method declared below is a derived query:
 * Spring Data generates its implementation automatically from the method
 * name.</p>
 */
@Repository
public interface AuthenticationRepository extends JpaRepository<Authentication, Integer> {
    /**
     * Finds the authentication record belonging to the user with the given id.
     *
     * <p>Traverses the {@code user} association and matches on its
     * {@code userId} property, as expressed by the {@code findByUser_UserId}
     * method name.</p>
     *
     * @param userId the id of the user whose credentials are sought
     * @return an {@link Optional} containing the matching authentication
     *         record, or an empty {@code Optional} if none exists for the user
     */
    Optional<Authentication> findByUser_UserId(Long userId);
}
