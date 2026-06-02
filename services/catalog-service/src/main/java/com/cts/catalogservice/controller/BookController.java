package com.cts.catalogservice.controller;

import com.cts.catalogservice.dto.request.BookRequest;
import com.cts.catalogservice.dto.response.BookResponse;
import com.cts.catalogservice.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


/**
 * REST controller for managing books in the catalog.
 *
 * <p>Supports CRUD operations, pagination, sorting, and search functionality.</p>
 */
@RestController
@RequestMapping("/api/catalog/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;


    /**
     * Retrieves paginated and sorted list of books.
     *
     * @param page        page number (default: 0)
     * @param pageSize    number of records (default: 5)
     * @param price       sorting field (default: price)
     * @param isAscending sort direction (true for ASC, false for DESC)
     * @return list of {@link BookResponse}
     */
    @GetMapping
    public ResponseEntity<List<BookResponse>> listBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "price") String price,
            @RequestParam(defaultValue = "true") boolean isAscending
            ) {
        return ResponseEntity.ok(bookService.listBooks(page, pageSize, price, isAscending));
    }


    /**
     * Retrieves a specific book by its ID.
     *
     * @param bookId book identifier
     * @return {@link BookResponse}
     */
    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse> getBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(bookService.getBook(bookId));
    }


    /**
     * Searches books using optional filters.
     *
     * @param title    book title (optional)
     * @param author   author name (optional)
     * @param category category name (optional)
     * @return filtered list of books
     */
    @GetMapping("/search")
    public ResponseEntity<List<BookResponse>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(bookService.searchBooks(title, author, category));
    }


    /**
     * Adds a new book to the catalog.
     *
     * @param request validated book request payload
     * @return created {@link BookResponse}
     */
    @PostMapping
    public ResponseEntity<BookResponse> addBook(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookService.addBook(request));
    }


    /**
     * Updates an existing book.
     *
     * @param bookId  book identifier
     * @param request updated book details
     * @return updated {@link BookResponse}
     */
    @PutMapping("/{bookId}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long bookId,
            @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookService.updateBook(bookId, request));
    }


    /**
     * Deletes a book by ID.
     *
     * @param bookId book identifier
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long bookId) {
        bookService.deleteBook(bookId);
        return ResponseEntity.noContent().build();
    }
}