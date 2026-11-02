package com.cts.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing inventory response details.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryResponseDTO {

    /**
     * Unique identifier of the inventory.
     */
    @NotNull(message = "Inventory ID cannot be null")
    private Long inventoryId;

    /**
     * Unique identifier of the book.
     */
    @NotNull(message = "Book ID cannot be null")
    private Long bookId;

    /**
     * Available quantity in inventory.
     */
    @NotNull(message = "Quantity cannot be null")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
}