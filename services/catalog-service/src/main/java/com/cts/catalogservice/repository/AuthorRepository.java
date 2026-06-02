package com.cts.catalogservice.repository;

import com.cts.catalogservice.model.Author;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Repository interface for performing CRUD and database operations on {@link Author}.
 *
 * <p>Extends {@link JpaRepository} to provide built-in methods such as
 * save, findById, findAll, and delete.</p>
 */
@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    /**
     * Finds an author by name (case-insensitive).
     *
     * @param authorName name of the author
     * @return optional containing the author if found, otherwise empty
     */
    Optional<Author> findByAuthorNameIgnoreCase(String authorName);


    /**
     * Checks if an author exists by name (case-insensitive).
     *
     * @param authorName name of the author
     * @return true if author exists, false otherwise
     */
    boolean existsByAuthorNameIgnoreCase(String authorName);
}
