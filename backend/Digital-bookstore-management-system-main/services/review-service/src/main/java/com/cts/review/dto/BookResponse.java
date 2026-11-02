package com.cts.review.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


/**
 * DTO representing book details fetched from the Catalog Service.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookResponse {

    /**
     * Unique identifier of the book.
     */
    @NotNull(message = "Book ID cannot be null")
    private Long bookId;


    /**
     * Title of the book.
     */
    @NotBlank(message = "Book title cannot be empty")
    private String title;


    /**
     * Price of the book.
     */
    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal price;


    /**
     * Name of the author of the book.
     */
    @NotBlank(message = "Author name cannot be empty")
    private String authorName;


    /**
     * Category or genre of the book.
     */
    @NotBlank(message = "Category name cannot be empty")
    private String categoryName;
}