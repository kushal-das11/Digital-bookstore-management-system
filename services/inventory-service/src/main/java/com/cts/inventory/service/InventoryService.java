package com.cts.inventory.service;

import com.cts.inventory.dto.AvailabilityDto;
import com.cts.inventory.dto.InventoryRequestDTO;
import com.cts.inventory.dto.InventoryResponseDTO;

import java.util.List;


/**
 * Service interface for managing inventory operations.
 */
public interface InventoryService {


    /**
     * Adds new inventory for a book.
     *
     * @param request inventory details
     * @return created inventory
     */
    InventoryResponseDTO addInventory(InventoryRequestDTO request);


    /**
     * Retrieves inventory by book ID.
     *
     * @param bookId book identifier
     * @return inventory details
     */
    InventoryResponseDTO getInventoryByBookId(long bookId);


    /**
     * Retrieves all inventory records.
     *
     * @return list of inventories
     */
    List<InventoryResponseDTO> getAllInventory();


    /**
     * Updates quantity for a book.
     *
     * @param bookId book identifier
     * @param quantity new quantity
     * @return updated inventory
     */
    InventoryResponseDTO updateQuantity(long bookId, int quantity);


    /**
     * Reduces stock for a book.
     *
     * @param bookId book identifier
     * @param quantity quantity to reduce
     */
    void reduceStock(long bookId, int quantity);


    /**
     * Deletes inventory for a book.
     *
     * @param bookId book identifier
     */
    void deleteInventory(long bookId);


    /**
     * Checks if stock is low.
     *
     * @param bookId book identifier
     * @return true if low, otherwise false
     */
    boolean isStockLow(long bookId);


    /**
     * Checks if requested quantity is unavailable.
     *
     * @param bookId book identifier
     * @param orderQty requested quantity
     * @return true if out of stock
     */
    boolean isOutOfStock(long bookId, int orderQty);


    /**
     * Gets stock availability details.
     *
     * @param bookId book identifier
     * @return availability information
     */
    AvailabilityDto getStockDetails(long bookId);


    /**
     * Retrieves all out-of-stock books.
     *
     * @return list of out-of-stock inventories
     */
    List<InventoryResponseDTO> allOutOfStockBooks();


    /**
     * Releases stock back to inventory.
     *
     * @param bookId book identifier
     * @param quantity quantity to add
     */
    void releaseStock(long bookId, int quantity);
}