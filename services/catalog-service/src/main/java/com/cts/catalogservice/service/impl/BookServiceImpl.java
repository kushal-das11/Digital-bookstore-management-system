package com.cts.catalogservice.service.impl;

import com.cts.catalogservice.client.InventoryClient;
import com.cts.catalogservice.dto.request.BookRequest;
import com.cts.catalogservice.dto.response.AvailabilityDto;
import com.cts.catalogservice.dto.response.BookResponse;
import com.cts.catalogservice.exception.author.AuthorNotFoundException;
import com.cts.catalogservice.exception.book.BookAlreadyExistsException;
import com.cts.catalogservice.exception.book.BookDeletionException;
import com.cts.catalogservice.exception.book.BookNotFoundException;
import com.cts.catalogservice.exception.book.InvalidBookDataException;
import com.cts.catalogservice.exception.category.CategoryNotFoundException;
import com.cts.catalogservice.exception.feignclientexception.InventoryServiceDownException;
import com.cts.catalogservice.model.Author;
import com.cts.catalogservice.model.Book;
import com.cts.catalogservice.model.Category;
import com.cts.catalogservice.repository.AuthorRepository;
import com.cts.catalogservice.repository.BookRepository;
import com.cts.catalogservice.repository.CategoryRepository;
import com.cts.catalogservice.service.BookService;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private static final int STOCK_UNAVAILABLE = -1;

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryClient inventoryClient;

    @Override
    @Transactional
    public BookResponse addBook(BookRequest request) {
        validatePrice(request.getPrice());

        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new AuthorNotFoundException(
                        "No author found with id " + request.getAuthorId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(
                        "No category found with id " + request.getCategoryId()));

        String title = request.getTitle().trim();
        if (bookRepository.existsByTitleIgnoreCaseAndAuthor(title, author)) {
            throw new BookAlreadyExistsException(
                    "Book \"" + title + "\" by " + author.getAuthorName() + " already exists");
        }

        Book saved = bookRepository.save(Book.builder()
                .title(title)
                .price(request.getPrice())
                .author(author)
                .category(category)
                .build());

        log.info("Created book id={} title={}", saved.getBookId(), saved.getTitle());
        return toResponse(saved, fetchStock(saved.getBookId()));
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long bookId, BookRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("No book found with id " + bookId));

        if (request.getPrice() != null) {
            validatePrice(request.getPrice());
            book.setPrice(request.getPrice());
        }

        if (StringUtils.hasText(request.getTitle())) {
            book.setTitle(request.getTitle().trim());
        }

        if (request.getAuthorId() != null
                && !request.getAuthorId().equals(book.getAuthor().getAuthorId())) {
            Author author = authorRepository.findById(request.getAuthorId())
                    .orElseThrow(() -> new AuthorNotFoundException(
                            "No author found with id " + request.getAuthorId()));
            book.setAuthor(author);
        }

        if (request.getCategoryId() != null
                && !request.getCategoryId().equals(book.getCategory().getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(
                            "No category found with id " + request.getCategoryId()));
            book.setCategory(category);
        }

        Book saved = bookRepository.save(book);
        log.info("Updated book id={}", saved.getBookId());
        return toResponse(saved, fetchStock(saved.getBookId()));
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("No book found with id " + bookId));
        try {
            bookRepository.delete(book);
            log.info("Deleted book id={}", bookId);
        } catch (DataAccessException ex) {
            throw new BookDeletionException("Failed to delete book id " + bookId, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "inventoryFallback")
    public BookResponse getBook(Long bookId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("No book found with id " + bookId));

        return toResponse(book, fetchStock(bookId));
    }

    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "listBooksFallback")
    public List<BookResponse> listBooks(Integer page, Integer pageSize, String fieldName, Boolean isAscending) {
        Sort sort = Boolean.TRUE.equals((isAscending)) ? Sort.by(fieldName).ascending() : Sort.by(fieldName).descending();
        Pageable pageable = PageRequest.of(
                page,
                pageSize,
                sort
        );
        Page<Book> books = bookRepository.findAll(pageable);
        return books.getContent().stream()
                .map(book -> toResponse(book, fetchStock(book.getBookId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> searchBooks(String title, String author, String category) {
        boolean hasTitle = StringUtils.hasText(title);
        boolean hasAuthor = StringUtils.hasText(author);
        boolean hasCategory = StringUtils.hasText(category);

        List<Book> results;
        if (hasCategory && hasTitle && hasAuthor) {
            results = bookRepository
                    .findByCategoryCategoryNameIgnoreCaseAndTitleContainingIgnoreCaseAndAuthorAuthorNameContainingIgnoreCase(
                            category, title, author);
        } else if (hasCategory && hasTitle) {
            results = bookRepository.findByCategoryCategoryNameIgnoreCaseAndTitleContainingIgnoreCase(category, title);
        } else if (hasCategory && hasAuthor) {
            results = bookRepository.findByCategoryCategoryNameIgnoreCaseAndAuthorAuthorNameContainingIgnoreCase(
                    category, author);
        } else if (hasTitle && hasAuthor) {
            results = bookRepository.findByTitleContainingIgnoreCaseAndAuthorAuthorNameContainingIgnoreCase(
                    title, author);
        } else if (hasCategory) {
            results = bookRepository.findByCategoryCategoryNameIgnoreCase(category);
        } else if (hasTitle) {
            results = bookRepository.findByTitleContainingIgnoreCase(title);
        } else if (hasAuthor) {
            results = bookRepository.findByAuthorAuthorNameContainingIgnoreCase(author);
        } else {
            results = bookRepository.findAll();
        }

        return results.stream()
                .map(b -> toResponse(b, fetchStock(b.getBookId())))
                .toList();
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBookDataException("Price must be greater than zero");
        }
    }

    private int fetchStock(Long bookId) {
        AvailabilityDto stock = inventoryClient.checkAvailability(bookId);
        return stock.getAvailableQuantity();
    }

    private BookResponse toResponse(Book b, int stock) {
        return BookResponse.builder()
                .bookId(b.getBookId())
                .title(b.getTitle())
                .price(b.getPrice())
                .authorName(b.getAuthor() != null ? b.getAuthor().getAuthorName() : null)
                .categoryName(b.getCategory() != null ? b.getCategory().getCategoryName() : null)
                .stockQuantity(stock)
                .build();
    }

    @Retry(name = "inventoryService", fallbackMethod = "inventoryFallback")
    @TimeLimiter(name = "inventoryService")
    private CompletableFuture<Integer> getAvailabilityDetails(Long bookId) {
        return CompletableFuture.supplyAsync(() -> fetchStock(bookId));
    }


    public CompletableFuture<AvailabilityDto> inventoryFallback(Long bookId, Throwable ex) {
        throw new InventoryServiceDownException("Inventory service unavailable for bookId: " + bookId + ex);
    }

    public List<BookResponse> listBooksFallback(Throwable ex) {

        log.error("Inventory service failed while listing books", ex);

        throw new InventoryServiceDownException("Inventory service unavailable");
    }

}
