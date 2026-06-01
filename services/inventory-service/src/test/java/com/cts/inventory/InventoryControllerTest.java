package com.cts.inventory;

import com.cts.inventory.controller.InventoryController;
import com.cts.inventory.dto.*;
import com.cts.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test class for {@link InventoryController}.
 * <p>
 * This class tests all REST endpoints exposed by the InventoryController.
 * It uses {@link WebMvcTest} to load only the web layer and mocks the
 * {@link InventoryService}.
 * </p>
 *
 * <p><b>Testing Approach:</b></p>
 * <ul>
 *     <li>Uses MockMvc for HTTP request simulation</li>
 *     <li>Mocks service layer to isolate controller logic</li>
 *     <li>Validates response status and payload</li>
 * </ul>
 */
@WebMvcTest(InventoryController.class)
@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService service;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Tests successful inventory creation.
     */
    @Test
    void addInventory_success() throws Exception {

        InventoryRequestDTO request = new InventoryRequestDTO();
        request.setBookId(100L);
        request.setQuantity(50);

        InventoryResponseDTO response = new InventoryResponseDTO();
        response.setBookId(100L);
        response.setQuantity(50);

        when(service.addInventory(org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(100L));
    }

    /**
     * Tests reducing stock successfully.
     */
    @Test
    void reduceStock_success() throws Exception {

        ReserveRequest request = new ReserveRequest(100L, 5);

        mockMvc.perform(put("/api/inventory/reduce")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Stock reduced successfully"));
    }

    /**
     * Tests releasing stock successfully.
     */
    @Test
    void releaseStock_success() throws Exception {

        ReserveRequest request = new ReserveRequest(100L, 5);

        mockMvc.perform(put("/api/inventory/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Stock released"));
    }

    /**
     * Tests fetching all out-of-stock books.
     */
    @Test
    void allOutOfStockBooks_success() throws Exception {

        when(service.allOutOfStockBooks())
                .thenReturn(List.of(new InventoryResponseDTO()));

        mockMvc.perform(get("/api/inventory/all-out-of-stock-books"))
                .andExpect(status().isOk());
    }

    /**
     * Tests fetching inventory by book ID.
     */
    @Test
    void getInventory_success() throws Exception {

        InventoryResponseDTO response = new InventoryResponseDTO();
        response.setBookId(100L);

        when(service.getInventoryByBookId(100L)).thenReturn(response);

        mockMvc.perform(get("/api/inventory/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(100L));
    }

    /**
     * Tests fetching all inventory records.
     */
    @Test
    void getAllInventory_success() throws Exception {

        when(service.getAllInventory())
                .thenReturn(List.of(new InventoryResponseDTO()));

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk());
    }

    /**
     * Tests updating inventory quantity.
     */
    @Test
    void updateQuantity_success() throws Exception {

        InventoryResponseDTO response = new InventoryResponseDTO();
        response.setQuantity(100);

        when(service.updateQuantity(100L, 100)).thenReturn(response);

        mockMvc.perform(put("/api/inventory/100")
                        .param("quantity", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(100));
    }

    /**
     * Tests deleting inventory.
     */
    @Test
    void deleteInventory_success() throws Exception {

        mockMvc.perform(delete("/api/inventory/100"))
                .andExpect(status().isOk())
                .andExpect(content().string("Inventory deleted successfully"));
    }

    /**
     * Tests checking low stock condition.
     */
    @Test
    void isStockLow_success() throws Exception {

        when(service.isStockLow(100L)).thenReturn(true);

        mockMvc.perform(get("/api/inventory/100/low-stock"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    /**
     * Tests retrieving stock availability details.
     */
    @Test
    void getStockDetails_success() throws Exception {

        AvailabilityDto dto = new AvailabilityDto();
        dto.setInStock(true);

        when(service.getStockDetails(100L)).thenReturn(dto);

        mockMvc.perform(get("/api/inventory/100/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inStock").value(true));
    }
}