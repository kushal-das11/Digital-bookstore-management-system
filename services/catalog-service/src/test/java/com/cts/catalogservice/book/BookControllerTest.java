package com.cts.catalogservice.book;

import com.cts.catalogservice.controller.BookController;
import com.cts.catalogservice.dto.request.BookRequest;
import com.cts.catalogservice.dto.response.BookResponse;
import com.cts.catalogservice.exception.GlobalExceptionHandler;
import com.cts.catalogservice.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for BookController.
 *
 * <p>Tests all REST endpoints including CRUD operations,
 * pagination, search functionality, validation, and service interactions.</p>
 */
@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private BookRequest request;
    private BookResponse response;

    /**
     * Initializes MockMvc and test data.
     */
    @BeforeEach
    void setup() {

        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        objectMapper = new ObjectMapper();

        request = new BookRequest("Java", BigDecimal.TEN, 1L, 1L);

        response = BookResponse.builder()
                .bookId(1L)
                .title("Java")
                .price(BigDecimal.TEN)
                .build();
    }

    /**
     * Tests retrieving paginated list of books.
     */
    @Test
    void listBooks_success() throws Exception {

        when(bookService.listBooks(anyInt(), anyInt(), anyString(), anyBoolean()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/catalog/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java"));

        verify(bookService).listBooks(anyInt(), anyInt(), anyString(), anyBoolean());
    }

    /**
     * Tests retrieving a book by ID.
     */
    @Test
    void getBook_success() throws Exception {

        when(bookService.getBook(1L)).thenReturn(response);

        mockMvc.perform(get("/api/catalog/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.title").value("Java"));

        verify(bookService).getBook(1L);
    }

    /**
     * Tests searching books using filters.
     */
    @Test
    void searchBooks_success() throws Exception {

        when(bookService.searchBooks(any(), any(), any()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/catalog/books/search")
                        .param("title", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java"));

        verify(bookService).searchBooks(any(), any(), any());
    }

    /**
     * Tests successful book creation.
     */
    @Test
    void addBook_success() throws Exception {

        when(bookService.addBook(any())).thenReturn(response);

        mockMvc.perform(post("/api/catalog/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Java"));

        verify(bookService).addBook(any());
    }

    /**
     * Tests validation failure during book creation.
     */
    @Test
    void addBook_validationFailure() throws Exception {

        request.setTitle("");

        mockMvc.perform(post("/api/catalog/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).addBook(any());
    }

    /**
     * Tests updating a book.
     */
    @Test
    void updateBook_success() throws Exception {

        when(bookService.updateBook(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/catalog/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java"));

        verify(bookService).updateBook(eq(1L), any());
    }

    /**
     * Tests deleting a book.
     */
    @Test
    void deleteBook_success() throws Exception {

        mockMvc.perform(delete("/api/catalog/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBook(1L);
    }
}