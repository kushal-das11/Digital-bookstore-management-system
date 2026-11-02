package com.cts.orderservice;

import com.cts.orderservice.controller.CartController;
import com.cts.orderservice.dto.request.CartRequest;
import com.cts.orderservice.dto.response.CartResponse;
import com.cts.orderservice.exception.GlobalExceptionHandler;
import com.cts.orderservice.exception.feignclientexception.CatalogServiceDownException;
import com.cts.orderservice.exception.order.CartItemNotFoundException;
import com.cts.orderservice.service.CartService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link CartController}.
 * Uses MockMvc standalone setup with mocked CartService.
 */
@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController controller;

    private MockMvc mockMvc;
    private ObjectMapper mapper;
    private CartResponse cartResponse;
    private CartRequest cartRequest;

    /**
     * Initializes MockMvc and sample test data before each test.
     */
    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();

        mapper = new ObjectMapper();
        cartResponse = new CartResponse(1L, 10L, 100L, "Clean Code", 2, LocalDateTime.now());

        cartRequest = new CartRequest();
        cartRequest.setBookId(100L);
        cartRequest.setQuantity(2);
    }

    // ================== ADD TO CART ==================

    /**
     * Tests successful addition of a book to the cart.
     */
    @Test
    void addToCart_success() throws Exception {
        when(cartService.addToCart(anyLong(), any())).thenReturn(cartResponse);

        mockMvc.perform(post("/api/orders/cart")
                        .header("X-User-Id", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(cartRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(100))
                .andExpect(jsonPath("$.quantity").value(2));

        verify(cartService).addToCart(anyLong(), any());
    }

    /**
     * Tests validation failure when bookId is null.
     */
    @Test
    void addToCart_missingBookId_returns400() throws Exception {
        cartRequest.setBookId(null);

        mockMvc.perform(post("/api/orders/cart")
                        .header("X-User-Id", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(cartRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addToCart(any(), any());
    }

    /**
     * Tests validation failure when quantity is zero or less.
     */
    @Test
    void addToCart_invalidQuantity_returns400() throws Exception {
        cartRequest.setQuantity(0);

        mockMvc.perform(post("/api/orders/cart")
                        .header("X-User-Id", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(cartRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addToCart(any(), any());
    }

    /**
     * Tests 503 response when catalog service is unavailable.
     */
    @Test
    void addToCart_catalogDown_returns503() throws Exception {
        when(cartService.addToCart(any(), any()))
                .thenThrow(new CatalogServiceDownException("Catalog service unavailable"));

        mockMvc.perform(post("/api/orders/cart")
                        .header("X-User-Id", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(cartRequest)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("CATALOG_SERVICE_DOWN"));
    }

    // ================== GET CART ==================

    /**
     * Tests successful retrieval of all cart items for a user.
     */
    @Test
    void getCart_success() throws Exception {
        when(cartService.getCart(anyLong())).thenReturn(List.of(cartResponse));

        mockMvc.perform(get("/api/orders/cart")
                        .header("X-User-Id", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(100))
                .andExpect(jsonPath("$[0].bookTitle").value("Clean Code"));

        verify(cartService).getCart(anyLong());
    }

    // ================== UPDATE CART ITEM ==================

    /**
     * Tests successful update of cart item quantity.
     */
    @Test
    void updateCartItem_success() throws Exception {
        cartRequest.setQuantity(5);
        cartResponse.setQuantity(5);
        when(cartService.updateCartItem(anyLong(), anyLong(), any())).thenReturn(cartResponse);

        mockMvc.perform(patch("/api/orders/cart/1")
                        .header("X-User-Id", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(cartRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));
    }

    /**
     * Tests 404 response when cart item does not exist.
     */
    @Test
    void updateCartItem_notFound_returns404() throws Exception {
        when(cartService.updateCartItem(anyLong(), anyLong(), any()))
                .thenThrow(new CartItemNotFoundException("Cart item not found: 1"));

        mockMvc.perform(patch("/api/orders/cart/1")
                        .header("X-User-Id", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(cartRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("CART_ITEM_NOT_FOUND"));
    }

    // ================== REMOVE FROM CART ==================

    /**
     * Tests successful removal of a cart item.
     */
    @Test
    void removeFromCart_success() throws Exception {
        mockMvc.perform(delete("/api/orders/cart/1")
                        .header("X-User-Id", 10L))
                .andExpect(status().isOk())
                .andExpect(content().string("Cart item removed."));

        verify(cartService).removeFromCart(anyLong(), anyLong());
    }

    /**
     * Tests 404 response when cart item belongs to a different user.
     */
    @Test
    void removeFromCart_notOwner_returns404() throws Exception {
        doThrow(new CartItemNotFoundException("Cart item does not belong to this user"))
                .when(cartService).removeFromCart(anyLong(), anyLong());

        mockMvc.perform(delete("/api/orders/cart/1")
                        .header("X-User-Id", 99L))
                .andExpect(status().isNotFound());
    }

    // ================== CLEAR CART ==================

    /**
     * Tests successful clearing of all cart items for a user.
     */
    @Test
    void clearCart_success() throws Exception {
        mockMvc.perform(delete("/api/orders/cart")
                        .header("X-User-Id", 10L))
                .andExpect(status().isOk())
                .andExpect(content().string("Cart cleared."));

        verify(cartService).clearCart(anyLong());
    }
}
