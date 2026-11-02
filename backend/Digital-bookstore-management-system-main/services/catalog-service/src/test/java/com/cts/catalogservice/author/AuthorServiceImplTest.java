package com.cts.catalogservice.author;

import com.cts.catalogservice.dto.request.AuthorRequest;
import com.cts.catalogservice.dto.response.AuthorResponse;
import com.cts.catalogservice.model.Author;
import com.cts.catalogservice.repository.AuthorRepository;
import com.cts.catalogservice.service.impl.AuthorServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthorServiceImpl}.
 *
 * <p>Validates creation, retrieval, and duplicate handling
 * behavior of AuthorService.</p>
 */
@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorServiceImpl authorService;

    /**
     * Verifies that a new author is created when no duplicate exists.
     */
    @Test
    void addAuthor_shouldCreateNewAuthor() {

        AuthorRequest request = new AuthorRequest("John");

        when(authorRepository.findByAuthorNameIgnoreCase("John"))
                .thenReturn(Optional.empty());

        when(authorRepository.save(any()))
                .thenReturn(new Author(1L, "John"));

        AuthorResponse response = authorService.addAuthor(request);

        assertEquals("John", response.getAuthorName());
        verify(authorRepository).save(any());
    }

    /**
     * Verifies that existing author is returned instead of creating duplicate.
     */
    @Test
    void addAuthor_shouldReturnExistingAuthor() {

        Author existing = new Author(1L, "John");

        when(authorRepository.findByAuthorNameIgnoreCase("John"))
                .thenReturn(Optional.of(existing));

        AuthorResponse response = authorService.addAuthor(new AuthorRequest("John"));

        assertEquals(1L, response.getAuthorId());
        verify(authorRepository, never()).save(any());
    }

    /**
     * Verifies listing of authors.
     */
    @Test
    void listAuthors_shouldReturnList() {

        when(authorRepository.findAll())
                .thenReturn(List.of(new Author(1L, "A")));

        List<AuthorResponse> result = authorService.listAuthors();

        assertEquals(1, result.size());
    }
}