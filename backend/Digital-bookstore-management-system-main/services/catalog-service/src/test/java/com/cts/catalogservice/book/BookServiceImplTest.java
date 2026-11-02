package com.cts.catalogservice.book;

import com.cts.catalogservice.client.InventoryClient;
import com.cts.catalogservice.dto.request.BookRequest;
import com.cts.catalogservice.dto.response.*;
import com.cts.catalogservice.exception.author.AuthorNotFoundException;
import com.cts.catalogservice.exception.book.*;
import com.cts.catalogservice.exception.category.CategoryNotFoundException;
import com.cts.catalogservice.exception.feignclientexception.InventoryServiceDownException;
import com.cts.catalogservice.model.*;
import com.cts.catalogservice.repository.*;
import com.cts.catalogservice.service.impl.BookServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookServiceImpl.
 *
 * <p>This class verifies all business logic scenarios including:
 * creation, update, deletion, retrieval, validation, and
 * integration with inventory service.</p>
 */
@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock private BookRepository bookRepository;
    @Mock private AuthorRepository authorRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private InventoryClient inventoryClient;

    @InjectMocks
    private BookServiceImpl bookService;

    private Author author = new Author(1L, "Author");
    private Category category = new Category(1L, "Tech");

    private Book book = Book.builder()
            .bookId(1L)
            .title("Java")
            .price(BigDecimal.valueOf(100))
            .author(author)
            .category(category)
            .build();

    /**
     * Verifies successful book creation.
     */
    @Test
    void addBook_success() {

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(bookRepository.existsByTitleIgnoreCaseAndAuthor("Java", author)).thenReturn(false);
        when(bookRepository.save(any())).thenReturn(book);

        AvailabilityDto dto = new AvailabilityDto();
        dto.setAvailableQuantity(10);
        when(inventoryClient.checkAvailability(1L)).thenReturn(dto);

        BookResponse res = bookService.addBook(
                new BookRequest("Java", BigDecimal.valueOf(100), 1L, 1L));

        assertEquals("Java", res.getTitle());
        assertEquals(10, res.getStockQuantity());
    }

    /**
     * Verifies author not found exception.
     */
    @Test
    void addBook_authorNotFound() {

        when(authorRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(AuthorNotFoundException.class,
                () -> bookService.addBook(new BookRequest("Java", BigDecimal.TEN, 1L, 1L)));
    }

    /**
     * Verifies category not found exception.
     */
    @Test
    void addBook_categoryNotFound() {

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> bookService.addBook(new BookRequest("Java", BigDecimal.TEN, 1L, 1L)));
    }

    /**
     * Verifies duplicate book scenario.
     */
    @Test
    void addBook_duplicate() {

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(bookRepository.existsByTitleIgnoreCaseAndAuthor("Java", author)).thenReturn(true);

        assertThrows(BookAlreadyExistsException.class,
                () -> bookService.addBook(new BookRequest("Java", BigDecimal.TEN, 1L, 1L)));
    }

    /**
     * Verifies invalid price validation.
     */
    @Test
    void addBook_invalidPrice() {

        assertThrows(InvalidBookDataException.class,
                () -> bookService.addBook(new BookRequest("Java", BigDecimal.ZERO, 1L, 1L)));
    }

    /**
     * Verifies fetching a single book.
     */
    @Test
    void getBook_success() {

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        AvailabilityDto dto = new AvailabilityDto();
        dto.setAvailableQuantity(5);

        when(inventoryClient.checkAvailability(1L)).thenReturn(dto);

        BookResponse res = bookService.getBook(1L);

        assertEquals(5, res.getStockQuantity());
    }

    /**
     * Verifies getBook throws exception when not found.
     */
    @Test
    void getBook_notFound() {

        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> bookService.getBook(1L));
    }

    /**
     * Verifies update book success.
     */
    @Test
    void updateBook_success() {

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any())).thenReturn(book);

        AvailabilityDto dto = new AvailabilityDto();
        dto.setAvailableQuantity(7);
        when(inventoryClient.checkAvailability(1L)).thenReturn(dto);

        BookResponse res = bookService.updateBook(1L,
                new BookRequest("Updated", BigDecimal.TEN, 1L, 1L));

        assertEquals("Updated", res.getTitle());
    }

    /**
     * Verifies delete success.
     */
    @Test
    void deleteBook_success() {

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.deleteBook(1L);

        verify(bookRepository).delete(book);
    }

    /**
     * Verifies delete failure handling.
     */
    @Test
    void deleteBook_failure() {

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        doThrow(BookDeletionException.class).when(bookRepository).delete(book);

        assertThrows(BookDeletionException.class,
                () -> bookService.deleteBook(1L));
    }

    /**
     * Verifies pagination and listing of books.
     */
    @Test
    void listBooks_success() {

        Page<Book> page = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(any(Pageable.class))).thenReturn(page);

        AvailabilityDto dto = new AvailabilityDto();
        dto.setAvailableQuantity(20);
        when(inventoryClient.checkAvailability(any())).thenReturn(dto);

        List<BookResponse> result =
                bookService.listBooks(0, 5, "price", true);

        assertEquals(1, result.size());
    }

    /**
     * Verifies search functionality fallback condition with no filters.
     */
    @Test
    void searchBooks_noFilters() {

        when(bookRepository.findAll()).thenReturn(List.of(book));

        AvailabilityDto dto = new AvailabilityDto();
        dto.setAvailableQuantity(10);
        when(inventoryClient.checkAvailability(any())).thenReturn(dto);

        List<BookResponse> result =
                bookService.searchBooks(null, null, null);

        assertFalse(result.isEmpty());
    }

    /**
     * Verifies inventory fallback exception.
     */
    @Test
    void inventoryFallback_shouldThrowException() {

        assertThrows(InventoryServiceDownException.class,
                () -> bookService.inventoryFallback(1L, new RuntimeException()));
    }

    /**
     * Verifies listBooks fallback behavior.
     */
    @Test
    void listBooksFallback_shouldThrowException() {

        assertThrows(InventoryServiceDownException.class,
                () -> bookService.listBooksFallback(new RuntimeException()));
    }
}