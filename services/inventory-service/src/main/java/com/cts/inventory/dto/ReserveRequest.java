package com.cts.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO used for reserving or releasing stock.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReserveRequest {

    /**
     * Unique identifier of the book.
     */
    @NotNull(message = "Book ID cannot be null")
    private Long bookId;

    /**
     * Quantity to reserve or release.
     */
    @Min(value = 1, message = "Quantity must be greater than zero")
    private int quantity;
}