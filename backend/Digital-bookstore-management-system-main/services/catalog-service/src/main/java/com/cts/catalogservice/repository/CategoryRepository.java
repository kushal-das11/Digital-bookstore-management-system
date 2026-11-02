package com.cts.catalogservice.repository;

import com.cts.catalogservice.model.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Repository interface for managing {@link Category} entities.
 *
 * <p>Provides CRUD operations and custom query methods for category data.</p>
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a category by name (case-insensitive).
     *
     * @param categoryName name of the category
     * @return optional containing category if found, otherwise empty
     */
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);


    /**
     * Checks if a category exists by name (case-insensitive).
     *
     * @param categoryName name of the category
     * @return true if exists, false otherwise
     */
    boolean existsByCategoryNameIgnoreCase(String categoryName);
}
