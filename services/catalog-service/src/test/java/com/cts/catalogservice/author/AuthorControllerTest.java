package com.cts.catalogservice.author;

import com.cts.catalogservice.controller.AuthorController;
import com.cts.catalogservice.dto.request.AuthorRequest;
import com.cts.catalogservice.dto.response.AuthorResponse;
import com.cts.catalogservice.exception.GlobalExceptionHandler;
import com.cts.catalogservice.service.AuthorService;
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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthorController.
 *
 * <p>This class verifies REST endpoints related to author management,
 * including successful operations, validation failures, and service interactions.</p>
 */
@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private AuthorController authorController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private AuthorRequest request;
    private AuthorResponse response;

    /**
     * Initializes test setup before each test execution.
     */
    @BeforeEach
    void setup() {

        mockMvc = MockMvcBuilders.standaloneSetup(authorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        objectMapper = new ObjectMapper();

        request = new AuthorRequest("John");
        response = new AuthorResponse(1L, "John");
    }

    /**
     * Verifies that all authors are retrieved successfully.
     */
    @Test
    void listAuthors_success() throws Exception {

        when(authorService.listAuthors()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/catalog/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].authorId").value(1))
                .andExpect(jsonPath("$[0].authorName").value("John"));

        verify(authorService).listAuthors();
    }

    /**
     * Verifies successful creation of an author.
     */
    @Test
    void addAuthor_success() throws Exception {

        when(authorService.addAuthor(any())).thenReturn(response);

        mockMvc.perform(post("/api/catalog/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorId").value(1))
                .andExpect(jsonPath("$.authorName").value("John"));

        verify(authorService).addAuthor(any());
    }

    /**
     * Verifies validation failure when request contains invalid data.
     */
    @Test
    void addAuthor_validationFailure() throws Exception {

        request.setAuthorName("");

        mockMvc.perform(post("/api/catalog/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authorService, never()).addAuthor(any());
    }
}