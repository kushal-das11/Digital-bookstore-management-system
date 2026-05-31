package com.cts.inventory.service;

import com.cts.inventory.dto.AvailabilityDto;
import com.cts.inventory.model.Inventory;
import com.cts.inventory.dto.InventoryRequestDTO;
import com.cts.inventory.dto.InventoryResponseDTO;

import java.util.List;

public interface InventoryService {

    InventoryResponseDTO addInventory(InventoryRequestDTO request);

    InventoryResponseDTO getInventoryByBookId(long bookId);

    List<InventoryResponseDTO> getAllInventory();

    InventoryResponseDTO updateQuantity(long bookId, int quantity);

    void reduceStock(long bookId, int quantity);

    void deleteInventory(long bookId);

    boolean isStockLow(long bookId);

    boolean isOutOfStock(long bookId, int orderQty);

    AvailabilityDto getStockDetails(long bookId);

    List<InventoryResponseDTO> allOutOfStockBooks();

    void releaseStock(long bookId, int quantity);
}