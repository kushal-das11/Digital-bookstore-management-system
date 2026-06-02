package com.cts.catalogservice.repository;

import com.cts.catalogservice.model.Author;
import com.cts.catalogservice.model.Book;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Repository interface for performing CRUD and custom queries on {@link Book}.
 *
 * <p>Includes pagination, existence checks, and flexible search methods
 * using Spring Data JPA query derivation.</p>
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Retrieves all books with pagination.
     *
     * @param pageable pagination information
     * @return paginated list of books
     */
    Page<Book> findAll(Pageable pageable);


    /**
     * Checks if a book exists by title and author.
     *
     * @param title  book title
     * @param author author entity
     * @return true if book exists, false otherwise
     */
    boolean existsByTitleIgnoreCaseAndAuthor(String title, Author author);


    /**
     * Finds books by partial title match (case-insensitive).
     *
     * @param title title keyword
     * @return list of matching books
     */
    List<Book> findByTitleContainingIgnoreCase(String title);


    /**
     * Finds books by partial author name match (case-insensitive).
     *
     * @param authorName author name keyword
     * @return list of matching books
     */
    List<Book> findByAuthorAuthorNameContainingIgnoreCase(String authorName);


    /**
     * Finds books by category name (case-insensitive).
     *
     * @param categoryName category name
     * @return list of matching books
     */
    List<Book> findByCategoryCategoryNameIgnoreCase(String categoryName);


    /**
     * Finds books by title and author name (case-insensitive).
     *
     * @param title      title keyword
     * @param authorName author name keyword
     * @return list of matching books
     */
    List<Book> findByTitleContainingIgnoreCaseAndAuthorAuthorNameContainingIgnoreCase(
            String title, String authorName);

    /**
     * Finds books by category and author name (case-insensitive).
     *
     * @param categoryName category name
     * @param authorName   author name keyword
     * @return list of matching books
     */
    List<Book> findByCategoryCategoryNameIgnoreCaseAndAuthorAuthorNameContainingIgnoreCase(
            String categoryName, String authorName);

    /**
     * Finds books by category and title.
     *
     * @param categoryName category name
     * @param title        title keyword
     * @return list of matching books
     */
    List<Book> findByCategoryCategoryNameIgnoreCaseAndTitleContainingIgnoreCase(
            String categoryName, String title);

    /**
     * Finds books by category, title, and author name.
     *
     * @param categoryName category name
     * @param title        title keyword
     * @param authorName   author name keyword
     * @return list of matching books
     */
    List<Book> findByCategoryCategoryNameIgnoreCaseAndTitleContainingIgnoreCaseAndAuthorAuthorNameContainingIgnoreCase(
            String categoryName, String title, String authorName);
}
