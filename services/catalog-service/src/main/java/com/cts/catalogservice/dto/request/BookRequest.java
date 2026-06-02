package com.cts.catalogservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
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
public class BookRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "authorId is required")
    private Long authorId;

    @NotNull(message = "categoryId is required")
    private Long categoryId;
}
