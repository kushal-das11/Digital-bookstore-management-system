package com.cts.inventory.controller;

import com.cts.inventory.dto.AvailabilityDto;
import com.cts.inventory.dto.InventoryRequestDTO;
import com.cts.inventory.dto.InventoryResponseDTO;
import com.cts.inventory.dto.ReserveRequest;
import com.cts.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * REST controller for handling inventory-related APIs.
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;


    /**
     * Creates new inventory.
     *
     * @param request inventory request data
     * @return created inventory details
     */
    @PostMapping
    public ResponseEntity<InventoryResponseDTO> addInventory(@RequestBody @Valid InventoryRequestDTO request) {

        log.info("Received request to create inventory for bookId={}, quantity={}",
                request.getBookId(), request.getQuantity());

        return ResponseEntity.ok(service.addInventory(request));
    }


    /**
     * Reduces stock based on request.
     *
     * @param reserveRequest request containing book ID and quantity
     * @return success message
     */
    @PutMapping("/reduce")
    public ResponseEntity<String> reduceStock(@RequestBody @Valid ReserveRequest reserveRequest) {
        log.info("Received request to reduce stock for bookId={}, quantity={}",
                reserveRequest.getBookId(), reserveRequest.getQuantity());
        service.reduceStock(reserveRequest.getBookId(), reserveRequest.getQuantity());
        return ResponseEntity.ok("Stock reduced successfully");
    }


    /**
     * Releases stock back to inventory.
     *
     * @param request request containing book ID and quantity
     * @return success message
     */
    @PutMapping("/release")
    public ResponseEntity<String> releaseStock(@RequestBody @Valid ReserveRequest request) {
        log.info("Received request to release stock for bookId={}, quantity={}",
                request.getBookId(), request.getQuantity());
        service.releaseStock(request.getBookId(), request.getQuantity());
        return ResponseEntity.ok("Stock released");
    }

    /**
     * Retrieves all out-of-stock books.
     *
     * @return list of out-of-stock inventories
     */
    @GetMapping("/all-out-of-stock-books")
    public ResponseEntity<List<InventoryResponseDTO>> allOutOfStockBooks() {
        log.info("Fetching all out-of-stock books");
        return ResponseEntity.ok(service.allOutOfStockBooks());
    }


    /**
     * Retrieves inventory by book ID.
     *
     * @param bookId book identifier
     * @return inventory details
     */
    @GetMapping("/{bookId:\\d+}")
    public ResponseEntity<InventoryResponseDTO> getInventory(@PathVariable Long bookId) {
        log.info("Fetching inventory for bookId={}", bookId);
        return ResponseEntity.ok(service.getInventoryByBookId(bookId));
    }


    /**
     * Retrieves all inventory records.
     *
     * @return list of inventories
     */
    @GetMapping
    public ResponseEntity<List<InventoryResponseDTO>> getAllInventory() {
        log.info("Fetching all inventory records");
        return ResponseEntity.ok(service.getAllInventory());
    }


    /**
     * Updates inventory quantity.
     *
     * @param bookId book identifier
     * @param quantity new quantity value
     * @return updated inventory details
     */
    @PutMapping("/{bookId:\\d+}")
    public ResponseEntity<InventoryResponseDTO> updateQuantity(
            @PathVariable Long bookId,
            @RequestParam int quantity) {
        log.info("Updating inventory for bookId={} with new quantity={}", bookId, quantity);
        return ResponseEntity.ok(service.updateQuantity(bookId, quantity));
    }

    /**
     * Deletes inventory by book ID.
     *
     * @param bookId book identifier
     * @return success message
     */
    @DeleteMapping("/{bookId:\\d+}")
    public ResponseEntity<String> deleteInventory(@PathVariable long bookId) {
        log.info("Deleting inventory for bookId={}", bookId);
        service.deleteInventory(bookId);
        return ResponseEntity.ok("Inventory deleted successfully");
    }

    /**
     * Checks if stock is low.
     *
     * @param bookId book identifier
     * @return true if stock is low
     */
    @GetMapping("/{bookId:\\d+}/low-stock")
    public ResponseEntity<Boolean> isStockLow(@PathVariable long bookId) {
        log.info("Checking low stock status for bookId={}", bookId);
        return ResponseEntity.ok(service.isStockLow(bookId));
    }


    /**
     * Retrieves stock availability details.
     *
     * @param bookId book identifier
     * @return availability information
     */
    @GetMapping("/{bookId:\\d+}/availability")
    public ResponseEntity<AvailabilityDto> getStockDetails(@PathVariable long bookId) {
        log.info("Fetching stock availability for bookId={}", bookId);
        return ResponseEntity.ok(service.getStockDetails(bookId));
    }
}