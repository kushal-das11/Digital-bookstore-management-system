package com.cts.catalogservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * DTO for creating or updating a Book.
 *
 * <p>Validation Rules:</p>
 * <ul>
 *     <li>title must not be blank (max 200 chars)</li>
 *     <li>price must be greater than 0</li>
 *     <li>authorId must not be null</li>
 *     <li>categoryId must not be null</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must be at most 100 characters")
    private String categoryName;
}
