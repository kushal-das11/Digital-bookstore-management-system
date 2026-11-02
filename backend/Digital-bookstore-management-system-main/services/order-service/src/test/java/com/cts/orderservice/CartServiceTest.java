package com.cts.orderservice;

import com.cts.orderservice.client.CatalogClient;
import com.cts.orderservice.dto.request.CartRequest;
import com.cts.orderservice.dto.response.BookResponse;
import com.cts.orderservice.dto.response.CartResponse;
import com.cts.orderservice.exception.order.CartItemNotFoundException;
import com.cts.orderservice.model.Cart;
import com.cts.orderservice.repository.CartRepository;
import com.cts.orderservice.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CartServiceImpl}.
 * Verifies cart business logic with mocked repository and catalog client.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cart;
    private BookResponse book;
    private CartRequest request;

    /**
     * Initializes sample cart, book, and request data before each test.
     */
    @BeforeEach
    void setUp() {
        book = new BookResponse(100L, "Clean Code", new BigDecimal("299.99"), "Robert Martin", "Programming");

        cart = new Cart();
        cart.setCartId(1L);
        cart.setUserId(10L);
        cart.setBookId(100L);
        cart.setBookTitle("Clean Code");
        cart.setQuantity(2);

        request = new CartRequest();
        request.setBookId(100L);
        request.setQuantity(2);
    }

    // ================== ADD TO CART ==================

    /**
     * Tests successful addition of a new book to the cart.
     */
    @Test
    void test_addToCart_newItem_success() {
        when(catalogClient.getBookById(100L)).thenReturn(book);
        when(cartRepository.findByUserIdAndBookId(10L, 100L)).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenReturn(cart);

        CartResponse response = cartService.addToCart(10L, request);

        assertNotNull(response);
        assertEquals(100L, response.getBookId());
        assertEquals("Clean Code", response.getBookTitle());
    }

    /**
     * Tests quantity increment when book already exists in cart.
     */
    @Test
    void test_addToCart_existingItem_updatesQuantity() {
        when(catalogClient.getBookById(100L)).thenReturn(book);
        when(cartRepository.findByUserIdAndBookId(10L, 100L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenReturn(cart);

        CartResponse response = cartService.addToCart(10L, request);

        assertNotNull(response);
        verify(cartRepository).save(any());
    }

    // ================== GET CART ==================

    /**
     * Tests successful retrieval of all cart items for a user.
     */
    @Test
    void test_getCart_success() {
        when(cartRepository.findByUserId(10L)).thenReturn(List.of(cart));

        List<CartResponse> result = cartService.getCart(10L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getBookId());
    }

    // ================== UPDATE CART ITEM ==================

    /**
     * Tests successful update of cart item quantity.
     */
    @Test
    void test_updateCartItem_success() {
        request.setQuantity(5);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenReturn(cart);

        CartResponse result = cartService.updateCartItem(10L, 1L, request);

        assertNotNull(result);
        verify(cartRepository).save(any());
    }

    /**
     * Tests CartItemNotFoundException when cart item does not exist.
     */
    @Test
    void test_updateCartItem_notFound() {
        when(cartRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CartItemNotFoundException.class,
                () -> cartService.updateCartItem(10L, 99L, request));
    }

    /**
     * Tests CartItemNotFoundException when cart item belongs to a different user.
     */
    @Test
    void test_updateCartItem_wrongUser() {
        cart.setUserId(999L);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        assertThrows(CartItemNotFoundException.class,
                () -> cartService.updateCartItem(10L, 1L, request));
    }

    // ================== REMOVE FROM CART ==================

    /**
     * Tests successful removal of a cart item by the owner.
     */
    @Test
    void test_removeFromCart_success() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        cartService.removeFromCart(10L, 1L);

        verify(cartRepository).deleteById(1L);
    }

    /**
     * Tests CartItemNotFoundException when cart item does not exist.
     */
    @Test
    void test_removeFromCart_notFound() {
        when(cartRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CartItemNotFoundException.class,
                () -> cartService.removeFromCart(10L, 99L));

        verify(cartRepository, never()).deleteById(any());
    }

    /**
     * Tests CartItemNotFoundException when cart item belongs to a different user.
     */
    @Test
    void test_removeFromCart_wrongUser() {
        cart.setUserId(999L);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        assertThrows(CartItemNotFoundException.class,
                () -> cartService.removeFromCart(10L, 1L));

        verify(cartRepository, never()).deleteById(any());
    }

    // ================== CLEAR CART ==================

    /**
     * Tests successful clearing of all cart items for a user.
     */
    @Test
    void test_clearCart_success() {
        cartService.clearCart(10L);
        verify(cartRepository).deleteByUserId(10L);
    }
}
