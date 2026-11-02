package com.cts.orderservice;

import com.cts.orderservice.controller.OrderController;
import com.cts.orderservice.dto.request.StatusRequest;
import com.cts.orderservice.dto.response.OrderResponse;
import com.cts.orderservice.exception.GlobalExceptionHandler;
import com.cts.orderservice.exception.feignclientexception.CatalogServiceDownException;
import com.cts.orderservice.exception.feignclientexception.InventoryServiceDownException;
import com.cts.orderservice.exception.inventory.InsufficientStockException;
import com.cts.orderservice.exception.order.InvalidOrderException;
import com.cts.orderservice.exception.order.InvalidOrderStatusException;
import com.cts.orderservice.exception.order.OrderNotFoundException;
import com.cts.orderservice.service.OrderService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link OrderController}.
 * Uses MockMvc standalone setup with mocked OrderService.
 */
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController controller;

    private MockMvc mockMvc;
    private ObjectMapper mapper;
    private OrderResponse orderResponse;

    /**
     * Initializes MockMvc and sample order data before each test.
     */
    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        mapper = new ObjectMapper();

        orderResponse = new OrderResponse();
        orderResponse.setOrderId(1L);
        orderResponse.setUserId(10L);
        orderResponse.setStatus("PLACED");
        orderResponse.setTotalAmount(new BigDecimal("499.99"));
        orderResponse.setOrderDate(LocalDateTime.now());
        orderResponse.setItems(List.of());
    }

    // ================== PLACE ORDER ==================

    /**
     * Tests successful order placement returning 201.
     */
    @Test
    void placeOrder_success() throws Exception {
        when(orderService.placeOrder(anyLong())).thenReturn(orderResponse);

        mockMvc.perform(post("/api/orders")
                        .header("X-User-Id", 10L))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.orderId").value(1));

        verify(orderService).placeOrder(anyLong());
    }

    /**
     * Tests 400 response when cart is empty.
     */
    @Test
    void placeOrder_emptyCart_returns400() throws Exception {
        when(orderService.placeOrder(anyLong()))
                .thenThrow(new InvalidOrderException("Cart is empty. Add items before ordering."));

        mockMvc.perform(post("/api/orders")
                        .header("X-User-Id", 10L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ORDER"));
    }

    /**
     * Tests 409 response when stock is insufficient.
     */
    @Test
    void placeOrder_insufficientStock_returns409() throws Exception {
        when(orderService.placeOrder(anyLong()))
                .thenThrow(new InsufficientStockException("Insufficient stock for bookId: 100"));

        mockMvc.perform(post("/api/orders")
                        .header("X-User-Id", 10L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"));
    }

    /**
     * Tests 503 response when catalog service is unavailable.
     */
    @Test
    void placeOrder_catalogDown_returns503() throws Exception {
        when(orderService.placeOrder(anyLong()))
                .thenThrow(new CatalogServiceDownException("Catalog service unavailable"));

        mockMvc.perform(post("/api/orders")
                        .header("X-User-Id", 10L))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("CATALOG_SERVICE_DOWN"));
    }

    /**
     * Tests 503 response when inventory service is unavailable.
     */
    @Test
    void placeOrder_inventoryDown_returns503() throws Exception {
        when(orderService.placeOrder(anyLong()))
                .thenThrow(new InventoryServiceDownException("Inventory service unavailable"));

        mockMvc.perform(post("/api/orders")
                        .header("X-User-Id", 10L))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("INVENTORY_SERVICE_DOWN"));
    }

    // ================== GET ORDER ==================

    /**
     * Tests successful retrieval of an order by ID.
     */
    @Test
    void getOrder_success() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(orderResponse);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("PLACED"));
    }

    /**
     * Tests 404 response when order is not found.
     */
    @Test
    void getOrder_notFound_returns404() throws Exception {
        when(orderService.getOrderById(99L))
                .thenThrow(new OrderNotFoundException("Order not found: 99"));

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"));
    }

    // ================== GET ORDERS BY USER ==================

    /**
     * Tests successful retrieval of all orders for a user.
     */
    @Test
    void getOrdersByUser_success() throws Exception {
        when(orderService.getOrdersByUserId(10L)).thenReturn(List.of(orderResponse));

        mockMvc.perform(get("/api/orders/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(10));
    }

    // ================== GET ORDER ITEMS ==================

    /**
     * Tests successful retrieval of items within an order.
     */
    @Test
    void getOrderItems_success() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(orderResponse);

        mockMvc.perform(get("/api/orders/1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1));
    }

    // ================== GET STATUS ==================

    /**
     * Tests successful retrieval of order status string.
     */
    @Test
    void getStatus_success() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(orderResponse);

        mockMvc.perform(get("/api/orders/1/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order #1 Status: PLACED"));
    }

    // ================== UPDATE STATUS ==================

    /**
     * Tests successful order status update.
     */
    @Test
    void updateStatus_success() throws Exception {
        StatusRequest req = new StatusRequest();
        req.setOrderId(1L);
        req.setStatus("SHIPPED");

        mockMvc.perform(patch("/api/orders/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("Status updated to SHIPPED"));

        verify(orderService).updateOrderStatus(1L, "SHIPPED");
    }

    /**
     * Tests 400 response when an invalid status value is provided.
     */
    @Test
    void updateStatus_invalidStatus_returns400() throws Exception {
        StatusRequest req = new StatusRequest();
        req.setOrderId(1L);
        req.setStatus("INVALID");

        doThrow(new InvalidOrderStatusException("Invalid status: INVALID"))
                .when(orderService).updateOrderStatus(1L, "INVALID");

        mockMvc.perform(patch("/api/orders/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_STATUS"));
    }

    /**
     * Tests 400 response when required status fields are missing.
     */
    @Test
    void updateStatus_missingFields_returns400() throws Exception {
        StatusRequest req = new StatusRequest();

        mockMvc.perform(patch("/api/orders/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateOrderStatus(any(), any());
    }

    // ================== CANCEL ORDER ==================

    /**
     * Tests successful cancellation of an order.
     */
    @Test
    void cancelOrder_success() throws Exception {
        mockMvc.perform(patch("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order #1 cancelled."));

        verify(orderService).cancelOrder(1L);
    }

    /**
     * Tests 400 response when cancelling a shipped order.
     */
    @Test
    void cancelOrder_shipped_returns400() throws Exception {
        doThrow(new InvalidOrderException("Cannot cancel a SHIPPED order"))
                .when(orderService).cancelOrder(1L);

        mockMvc.perform(patch("/api/orders/1/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ORDER"));
    }

    /**
     * Tests 400 response when cancelling a delivered order.
     */
    @Test
    void cancelOrder_delivered_returns400() throws Exception {
        doThrow(new InvalidOrderException("Cannot cancel a DELIVERED order"))
                .when(orderService).cancelOrder(1L);

        mockMvc.perform(patch("/api/orders/1/cancel"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests 400 response when order is already cancelled.
     */
    @Test
    void cancelOrder_alreadyCancelled_returns400() throws Exception {
        doThrow(new InvalidOrderException("Order is already cancelled"))
                .when(orderService).cancelOrder(1L);

        mockMvc.perform(patch("/api/orders/1/cancel"))
                .andExpect(status().isBadRequest());
    }

    // ================== GET ALL ORDERS ==================

    /**
     * Tests successful retrieval of all orders (admin).
     */
    @Test
    void getAllOrders_success() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(orderResponse));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(1));
    }
}
