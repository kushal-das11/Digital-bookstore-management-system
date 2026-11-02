package com.cts.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating inventory details.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRequestDTO {

    /**
     * Unique identifier of the book.
     */
    @NotNull(message = "Book ID cannot be null")
    private Long bookId;

    /**
     * Quantity to be added to inventory.
     */
    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be greater than zero")
    private Integer quantity;
}