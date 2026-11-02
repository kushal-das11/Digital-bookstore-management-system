package com.cts.inventory;

import com.cts.inventory.controller.InventoryController;
import com.cts.inventory.dto.*;
import com.cts.inventory.exception.GlobalExceptionHandler;
import com.cts.inventory.exception.customexception.*;
import com.cts.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for InventoryController.
 *
 * This class verifies all REST endpoints, including
 * success scenarios, validation behavior, and exception handling.
 */
@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryService service;

    @InjectMocks
    private InventoryController controller;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    private InventoryRequestDTO request;
    private InventoryResponseDTO response;

    /**
     * Initializes MockMvc and test data before each test.
     */
    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        mapper = new ObjectMapper();

        request = new InventoryRequestDTO(100L, 10);
        response = new InventoryResponseDTO(1L, 100L, 10);
    }

    /**
     * Tests successful inventory creation.
     */
    @Test
    void addInventory_success() throws Exception {

        when(service.addInventory(any())).thenReturn(response);

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(100))
                .andExpect(jsonPath("$.quantity").value(10));

        verify(service).addInventory(any());
    }

    /**
     * Tests validation failure when quantity is invalid.
     */
    @Test
    void addInventory_validationFailure_returns400() throws Exception {

        request.setQuantity(0);

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(service, never()).addInventory(any());
    }

    /**
     * Tests service-level exception during inventory creation.
     */
    @Test
    void addInventory_serviceException_returns400() throws Exception {

        when(service.addInventory(any()))
                .thenThrow(new InvalidInventoryException("Invalid"));

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests successful stock reduction.
     */
    @Test
    void reduceStock_success() throws Exception {

        ReserveRequest reserve = new ReserveRequest(100L, 2);

        mockMvc.perform(put("/api/inventory/reduce")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reserve)))
                .andExpect(status().isOk())
                .andExpect(content().string("Stock reduced successfully"));

        verify(service).reduceStock(100L, 2);
    }

    /**
     * Tests validation failure during stock reduction.
     */
    @Test
    void reduceStock_validationFailure_returns400() throws Exception {

        ReserveRequest reserve = new ReserveRequest(100L, 0);

        mockMvc.perform(put("/api/inventory/reduce")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reserve)))
                .andExpect(status().isBadRequest());

        verify(service, never()).reduceStock(anyLong(), anyInt());
    }

    /**
     * Tests out-of-stock scenario during stock reduction.
     */
    @Test
    void reduceStock_outOfStock_returns409() throws Exception {

        ReserveRequest reserve = new ReserveRequest(100L, 50);

        doThrow(new OutOfStockException("Not enough stock"))
                .when(service).reduceStock(100L, 50);

        mockMvc.perform(put("/api/inventory/reduce")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reserve)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Not enough stock"));
    }

    /**
     * Tests successful stock release.
     */
    @Test
    void releaseStock_success() throws Exception {

        ReserveRequest reserve = new ReserveRequest(100L, 2);

        mockMvc.perform(put("/api/inventory/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reserve)))
                .andExpect(status().isOk())
                .andExpect(content().string("Stock released"));

        verify(service).releaseStock(100L, 2);
    }

    /**
     * Tests validation failure during stock release.
     */
    @Test
    void releaseStock_validationFailure_returns400() throws Exception {

        ReserveRequest reserve = new ReserveRequest(100L, 0);

        mockMvc.perform(put("/api/inventory/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reserve)))
                .andExpect(status().isBadRequest());

        verify(service, never()).releaseStock(anyLong(), anyInt());
    }

    /**
     * Tests fetching inventory by book ID.
     */
    @Test
    void getInventory_success() throws Exception {

        when(service.getInventoryByBookId(100L)).thenReturn(response);

        mockMvc.perform(get("/api/inventory/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(100));
    }

    /**
     * Tests inventory not found scenario.
     */
    @Test
    void getInventory_notFound_returns404() throws Exception {

        when(service.getInventoryByBookId(100L))
                .thenThrow(new InventoryNotFoundException("Not found"));

        mockMvc.perform(get("/api/inventory/100"))
                .andExpect(status().isNotFound());
    }

    /**
     * Tests retrieving all inventory records.
     */
    @Test
    void getAllInventory_success() throws Exception {

        when(service.getAllInventory()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(100));
    }

    /**
     * Tests retrieving all out-of-stock inventories.
     */
    @Test
    void getAllOutOfStock_success() throws Exception {

        when(service.allOutOfStockBooks()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/inventory/all-out-of-stock-books"))
                .andExpect(status().isOk());
    }

    /**
     * Tests updating inventory quantity successfully.
     */
    @Test
    void updateQuantity_success() throws Exception {

        when(service.updateQuantity(100L, 20))
                .thenReturn(new InventoryResponseDTO(1L, 100L, 20));

        mockMvc.perform(put("/api/inventory/100")
                        .param("quantity", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(20));
    }

    /**
     * Tests update failure when inventory is not found.
     */
    @Test
    void updateQuantity_notFound_returns404() throws Exception {

        when(service.updateQuantity(100L, 20))
                .thenThrow(new InventoryNotFoundException("Not found"));

        mockMvc.perform(put("/api/inventory/100")
                        .param("quantity", "20"))
                .andExpect(status().isNotFound());
    }

    /**
     * Tests successful deletion of inventory.
     */
    @Test
    void deleteInventory_success() throws Exception {

        mockMvc.perform(delete("/api/inventory/100"))
                .andExpect(status().isOk());

        verify(service).deleteInventory(100L);
    }

    /**
     * Tests deletion failure when inventory does not exist.
     */
    @Test
    void deleteInventory_notFound_returns404() throws Exception {

        doThrow(new InventoryNotFoundException("Not found"))
                .when(service).deleteInventory(100L);

        mockMvc.perform(delete("/api/inventory/100"))
                .andExpect(status().isNotFound());
    }

    /**
     * Tests checking low-stock condition.
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

        AvailabilityDto dto = new AvailabilityDto(100L, 10, true);

        when(service.getStockDetails(100L)).thenReturn(dto);

        mockMvc.perform(get("/api/inventory/100/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(100));
    }
}