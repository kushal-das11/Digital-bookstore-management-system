package com.cts.inventory.controller;

import com.cts.inventory.dto.AvailabilityDto;
import com.cts.inventory.dto.InventoryRequestDTO;
import com.cts.inventory.dto.InventoryResponseDTO;
import com.cts.inventory.dto.ReserveRequest;
import com.cts.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    @PostMapping
    public ResponseEntity<InventoryResponseDTO> addInventory(@RequestBody InventoryRequestDTO request) {
        return ResponseEntity.ok(service.addInventory(request));
    }

    @PutMapping("/reduce")
    public ResponseEntity<String> reduceStock(@RequestBody ReserveRequest reserveRequest) {
        service.reduceStock(reserveRequest.getBookId(), reserveRequest.getQuantity());
        return ResponseEntity.ok("Stock reduced successfully");
    }

    @PutMapping("/release")
    public ResponseEntity<String> releaseStock(@RequestBody ReserveRequest request) {
        service.releaseStock(request.getBookId(), request.getQuantity());
        return ResponseEntity.ok("Stock released");
    }

    @GetMapping("/all-out-of-stock-books")
    public ResponseEntity<List<InventoryResponseDTO>> allOutOfStockBooks() {
        return ResponseEntity.ok(service.allOutOfStockBooks());
    }

    @GetMapping("/{bookId:\\d+}")
    public ResponseEntity<InventoryResponseDTO> getInventory(@PathVariable Long bookId) {
        return ResponseEntity.ok(service.getInventoryByBookId(bookId));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponseDTO>> getAllInventory() {
        return ResponseEntity.ok(service.getAllInventory());
    }

    @PutMapping("/{bookId:\\d+}")
    public ResponseEntity<InventoryResponseDTO> updateQuantity(
            @PathVariable Long bookId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(service.updateQuantity(bookId, quantity));
    }

    @DeleteMapping("/{bookId:\\d+}")
    public ResponseEntity<String> deleteInventory(@PathVariable long bookId) {
        service.deleteInventory(bookId);
        return ResponseEntity.ok("Inventory deleted successfully");
    }

    @GetMapping("/{bookId:\\d+}/low-stock")
    public ResponseEntity<Boolean> isStockLow(@PathVariable long bookId) {
        return ResponseEntity.ok(service.isStockLow(bookId));
    }

    @GetMapping("/{bookId:\\d+}/availability")
    public ResponseEntity<AvailabilityDto> getStockDetails(@PathVariable long bookId) {
        return ResponseEntity.ok(service.getStockDetails(bookId));
    }
}