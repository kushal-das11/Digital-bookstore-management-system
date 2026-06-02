package com.cts.catalogservice.repository;

import com.cts.catalogservice.model.Author;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findByAuthorNameIgnoreCase(String authorName);

    boolean existsByAuthorNameIgnoreCase(String authorName);
}
