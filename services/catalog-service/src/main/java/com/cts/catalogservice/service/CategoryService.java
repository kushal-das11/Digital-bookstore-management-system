package com.cts.catalogservice.service;

import com.cts.catalogservice.dto.request.CategoryRequest;
import com.cts.catalogservice.dto.response.CategoryResponse;
import java.util.List;

public interface CategoryService {

    CategoryResponse addCategory(CategoryRequest request);

    List<CategoryResponse> listCategories();
}
