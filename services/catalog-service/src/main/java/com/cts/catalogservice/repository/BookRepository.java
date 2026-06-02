package com.cts.catalogservice.repository;

import com.cts.catalogservice.model.Author;
import com.cts.catalogservice.model.Book;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Page<Book> findAll(Pageable pageable);

    boolean existsByTitleIgnoreCaseAndAuthor(String title, Author author);

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByAuthorAuthorNameContainingIgnoreCase(String authorName);

    List<Book> findByCategoryCategoryNameIgnoreCase(String categoryName);

    List<Book> findByTitleContainingIgnoreCaseAndAuthorAuthorNameContainingIgnoreCase(
            String title, String authorName);

    List<Book> findByCategoryCategoryNameIgnoreCaseAndAuthorAuthorNameContainingIgnoreCase(
            String categoryName, String authorName);

    List<Book> findByCategoryCategoryNameIgnoreCaseAndTitleContainingIgnoreCase(
            String categoryName, String title);

    List<Book> findByCategoryCategoryNameIgnoreCaseAndTitleContainingIgnoreCaseAndAuthorAuthorNameContainingIgnoreCase(
            String categoryName, String title, String authorName);
}
