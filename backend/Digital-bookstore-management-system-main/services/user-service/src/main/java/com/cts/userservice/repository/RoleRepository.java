package com.cts.userservice.repository;

import com.cts.userservice.model.Role;
import com.cts.userservice.model.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * Spring Data JPA repository for {@link Role} entities.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD and pagination
 * operations for {@code Role}, whose primary key is of type {@link Integer}.
 * The additional method declared below is a derived query: Spring Data
 * generates its implementation automatically from the method name.</p>
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    /**
     * Finds a role by its name.
     *
     * @param roleName the {@link RoleName} to search for
     * @return an {@link Optional} containing the matching role, or an empty
     *         {@code Optional} if no role has the given name
     */
    Optional<Role> findByRoleName(RoleName roleName);
}
