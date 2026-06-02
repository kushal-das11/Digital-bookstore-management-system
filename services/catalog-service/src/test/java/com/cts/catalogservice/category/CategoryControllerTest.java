package com.cts.catalogservice.category;

import com.cts.catalogservice.controller.CategoryController;
import com.cts.catalogservice.dto.request.CategoryRequest;
import com.cts.catalogservice.dto.response.CategoryResponse;
import com.cts.catalogservice.exception.GlobalExceptionHandler;
import com.cts.catalogservice.service.CategoryService;
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
 * Unit tests for CategoryController.
 *
 * <p>This class verifies REST endpoints for category management,
 * including successful operations, validation failures, and
 * service interactions.</p>
 */
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private CategoryRequest request;
    private CategoryResponse response;

    /**
     * Initializes test setup before each test execution.
     */
    @BeforeEach
    void setup() {

        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        objectMapper = new ObjectMapper();

        request = new CategoryRequest("Fiction");
        response = new CategoryResponse(1L, "Fiction");
    }

    /**
     * Verifies that all categories are retrieved successfully.
     */
    @Test
    void listCategories_success() throws Exception {

        when(categoryService.listCategories())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/catalog/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value(1L))
                .andExpect(jsonPath("$[0].categoryName").value("Fiction"));

        verify(categoryService).listCategories();
    }

    /**
     * Verifies successful creation of a category.
     */
    @Test
    void addCategory_success() throws Exception {

        when(categoryService.addCategory(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/catalog/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(1L))
                .andExpect(jsonPath("$.categoryName").value("Fiction"));

        verify(categoryService).addCategory(any());
    }

    /**
     * Verifies validation failure when request contains invalid data.
     */
    @Test
    void addCategory_validationFailure() throws Exception {

        request.setCategoryName("");

        mockMvc.perform(post("/api/catalog/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).addCategory(any());
    }
}