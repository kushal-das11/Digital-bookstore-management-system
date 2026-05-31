package com.cts.inventory;

import com.cts.inventory.dto.*;
import com.cts.inventory.exception.customexception.*;
import com.cts.inventory.model.Inventory;
import com.cts.inventory.repository.InventoryRepository;
import com.cts.inventory.service.impl.InventoryServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryTest {

    @Mock
    private InventoryRepository repository;

    @InjectMocks
    private InventoryServiceImpl service;

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = Inventory.builder()
                .inventoryId(1L)
                .bookId(100L)
                .quantity(10)
                .build();
    }

    // ================== ADD INVENTORY ==================
    @Test
    void test_addInventory_success() {
        InventoryRequestDTO request = new InventoryRequestDTO(100L, 10);

        when(repository.existsByBookId(100L)).thenReturn(false);
        when(repository.save(any())).thenReturn(inventory);

        InventoryResponseDTO response = service.addInventory(request);

        assertNotNull(response);
        assertEquals(100L, response.getBookId());
    }

    @Test
    void test_addInventory_invalidQuantity() {
        InventoryRequestDTO request = new InventoryRequestDTO(100L, 0);

        assertThrows(InvalidInventoryException.class,
                () -> service.addInventory(request));
    }

    @Test
    void test_addInventory_duplicate() {
        when(repository.existsByBookId(100L)).thenReturn(true);

        InventoryRequestDTO request = new InventoryRequestDTO(100L, 5);

        assertThrows(InvalidInventoryException.class,
                () -> service.addInventory(request));
    }

    // ================== GET BY BOOK ID ==================
    @Test
    void test_getInventory_success() {
        when(repository.findByBookId(100L))
                .thenReturn(Optional.of(inventory));

        InventoryResponseDTO response = service.getInventoryByBookId(100L);

        assertEquals(100L, response.getBookId());
    }

    @Test
    void test_getInventory_notFound() {
        when(repository.findByBookId(100L))
                .thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> service.getInventoryByBookId(100L));
    }

    // ================== GET ALL ==================
    @Test
    void test_getAllInventory() {
        when(repository.findAll()).thenReturn(List.of(inventory));

        List<InventoryResponseDTO> list = service.getAllInventory();

        assertEquals(1, list.size());
    }

    // ================== UPDATE ==================
    @Test
    void test_updateQuantity_success() {
        when(repository.findByBookId(100L))
                .thenReturn(Optional.of(inventory));

        when(repository.save(any())).thenReturn(inventory);

        InventoryResponseDTO response = service.updateQuantity(100L, 5);

        assertEquals(100L, response.getBookId());
    }

    @Test
    void test_updateQuantity_invalid() {
        assertThrows(InvalidInventoryException.class,
                () -> service.updateQuantity(100L, -1));
    }

    @Test
    void test_updateQuantity_notFound() {
        when(repository.findByBookId(100L))
                .thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> service.updateQuantity(100L, 5));
    }

    // ================== DELETE ==================
    @Test
    void test_delete_success() {
        when(repository.existsByBookId(100L)).thenReturn(true);

        service.deleteInventory(100L);

        verify(repository).deleteByBookId(100L);
    }

    @Test
    void test_delete_notFound() {
        when(repository.existsByBookId(100L)).thenReturn(false);

        assertThrows(InventoryNotFoundException.class,
                () -> service.deleteInventory(100L));
    }

    // ================== STOCK CHECK ==================
    @Test
    void test_isStockLow_true() {
        inventory.setQuantity(0);

        when(repository.findByBookId(100L))
                .thenReturn(Optional.of(inventory));

        assertTrue(service.isStockLow(100L));
    }

    @Test
    void test_isStockLow_notFound() {
        when(repository.findByBookId(100L))
                .thenReturn(Optional.empty());

        assertTrue(service.isStockLow(100L));
    }

    // ================== STOCK DETAILS ==================
    @Test
    void test_getStockDetails_success() {
        when(repository.findByBookId(100L))
                .thenReturn(Optional.of(inventory));

        AvailabilityDto dto = service.getStockDetails(100L);

        assertEquals(100L, dto.getBookId());
        assertTrue(dto.isInStock());
    }

    // ================== OUT OF STOCK ==================
    @Test
    void test_isOutOfStock_true() {
        inventory.setQuantity(2);

        when(repository.findByBookId(100L))
                .thenReturn(Optional.of(inventory));

        assertTrue(service.isOutOfStock(100L, 5));
    }

    // ================== REDUCE STOCK ==================
    @Test
    void test_reduceStock_success() {
        when(repository.findByBookIdForUpdate(100L))
                .thenReturn(Optional.of(inventory));

        service.reduceStock(100L, 5);

        verify(repository).save(any());
    }

    @Test
    void test_reduceStock_insufficient() {
        inventory.setQuantity(2);

        when(repository.findByBookIdForUpdate(100L))
                .thenReturn(Optional.of(inventory));

        assertThrows(OutOfStockException.class,
                () -> service.reduceStock(100L, 5));
    }

    // ================== RELEASE STOCK ==================
    @Test
    void test_releaseStock_success() {
        when(repository.findByBookIdForUpdate(100L))
                .thenReturn(Optional.of(inventory));

        service.releaseStock(100L, 5);

        verify(repository).save(any());
    }

    // ================== OUT OF STOCK LIST ==================
    @Test
    void test_allOutOfStockBooks() {
        inventory.setQuantity(0);

        when(repository.findAll())
                .thenReturn(List.of(inventory));

        List<InventoryResponseDTO> list = service.allOutOfStockBooks();

        assertEquals(1, list.size());
    }
}