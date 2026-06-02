package com.cts.catalogservice.category;

import com.cts.catalogservice.dto.request.CategoryRequest;
import com.cts.catalogservice.dto.response.CategoryResponse;
import com.cts.catalogservice.model.Category;
import com.cts.catalogservice.repository.CategoryRepository;
import com.cts.catalogservice.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CategoryServiceImpl}.
 *
 * <p>Ensures correct behavior for creating and retrieving categories,
 * including duplicate handling.</p>
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    /**
     * Verifies new category creation when it does not already exist.
     */
    @Test
    void addCategory_shouldCreateNew() {

        when(categoryRepository.findByCategoryNameIgnoreCase("Fiction"))
                .thenReturn(Optional.empty());

        when(categoryRepository.save(any()))
                .thenReturn(new Category(1L, "Fiction"));

        CategoryResponse res = categoryService.addCategory(new CategoryRequest("Fiction"));

        assertEquals("Fiction", res.getCategoryName());
    }

    /**
     * Verifies existing category is returned when duplicate found.
     */
    @Test
    void addCategory_shouldReturnExisting() {

        when(categoryRepository.findByCategoryNameIgnoreCase("Fiction"))
                .thenReturn(Optional.of(new Category(1L, "Fiction")));

        CategoryResponse res = categoryService.addCategory(new CategoryRequest("Fiction"));

        assertEquals(1L, res.getCategoryId());
        verify(categoryRepository, never()).save(any());
    }

    /**
     * Verifies retrieval of all categories.
     */
    @Test
    void listCategories_shouldReturnList() {

        when(categoryRepository.findAll())
                .thenReturn(List.of(new Category(1L, "Fiction")));

        assertEquals(1, categoryService.listCategories().size());
    }
}