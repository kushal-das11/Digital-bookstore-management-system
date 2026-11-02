package com.cts.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing stock availability details of a book.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityDto {

    /**
     * Unique identifier of the book.
     */
    @NotNull(message = "Book ID cannot be null")
    private Long bookId;

    /**
     * Available quantity in inventory.
     */
    @Min(value = 0, message = "Available quantity cannot be negative")
    private int availableQuantity;

    /**
     * Indicates whether the book is in stock.
     */
    @NotNull(message = "Stock status cannot be null")
    private boolean inStock;
}

