package com.cts.orderservice.service;

import com.cts.orderservice.dto.request.CartRequest;
import com.cts.orderservice.dto.response.CartResponse;
import java.util.List;

/**
 * Service interface for cart operations.
 */
public interface CartService {

    /** Adds a book to user cart. */
    CartResponse addToCart(Long userId, CartRequest request);

    /** Returns all cart items for the user. */
    List<CartResponse> getCart(Long userId);

    /** Updates quantity of an existing cart item. */
    CartResponse updateCartItem(Long userId, Long cartId, CartRequest request);

    /** Removes a specific cart item. */
    void removeFromCart(Long userId, Long cartId);

    /** Clears all cart items for the user. */
    void clearCart(Long userId);
}