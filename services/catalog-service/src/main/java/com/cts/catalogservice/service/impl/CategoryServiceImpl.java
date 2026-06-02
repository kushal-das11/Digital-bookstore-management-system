package com.cts.catalogservice.service.impl;

import com.cts.catalogservice.dto.request.CategoryRequest;
import com.cts.catalogservice.dto.response.CategoryResponse;
import com.cts.catalogservice.model.Category;
import com.cts.catalogservice.repository.CategoryRepository;
import com.cts.catalogservice.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse addCategory(CategoryRequest request) {
        String name = request.getCategoryName().trim();
        return categoryRepository.findByCategoryNameIgnoreCase(name)
                .map(this::toResponse)
                .orElseGet(() -> {
                    Category saved = categoryRepository.save(Category.builder().categoryName(name).build());
                    log.info("Created category id={} name={}", saved.getCategoryId(), saved.getCategoryName());
                    return toResponse(saved);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .categoryId(c.getCategoryId())
                .categoryName(c.getCategoryName())
                .build();
    }
}
