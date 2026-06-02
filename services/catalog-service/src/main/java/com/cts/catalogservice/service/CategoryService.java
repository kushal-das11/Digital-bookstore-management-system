package com.cts.catalogservice.service;

import com.cts.catalogservice.dto.request.CategoryRequest;
import com.cts.catalogservice.dto.response.CategoryResponse;
import java.util.List;


/**
 * Service interface for managing category-related operations.
 *
 * <p>Provides methods for creating and retrieving categories.</p>
 */
public interface CategoryService {


    /**
     * Adds a new category or returns an existing one
     * if it already exists (case-insensitive match).
     *
     * @param request category creation request
     * @return created or existing {@link CategoryResponse}
     */
    CategoryResponse addCategory(CategoryRequest request);


    /**
     * Retrieves all categories.
     *
     * @return list of {@link CategoryResponse}
     */
    List<CategoryResponse> listCategories();
}
