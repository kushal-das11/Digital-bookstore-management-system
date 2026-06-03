package com.cts.orderservice.controller;

import com.cts.orderservice.dto.request.CartRequest;
import com.cts.orderservice.dto.response.CartResponse;
import com.cts.orderservice.service.CartService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for cart operations.
 */
@RestController
@RequestMapping("/api/orders/cart")
@Slf4j
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /** POST — Add book to cart. Body: {bookId, quantity} */
    @PostMapping
    public ResponseEntity<CartResponse> addToCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody CartRequest request) {
        log.info("Add to cart: userId={} bookId={}",
                userId, request.getBookId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addToCart(userId, request));
    }

    /** GET — View cart items. */
    @GetMapping
    public ResponseEntity<List<CartResponse>> getCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("Get cart: userId={}", userId);
        return ResponseEntity.ok(
                cartService.getCart(userId));
    }

    /** PATCH — Update quantity. Body: {quantity} */
    @PatchMapping("/{cartId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long cartId,
            @Valid @RequestBody CartRequest request) {
        log.info("Update cart: userId={} cartId={}", userId, cartId);
        return ResponseEntity.ok(
                cartService.updateCartItem(userId, cartId, request));
    }

    /** DELETE — Remove one item. */
    @DeleteMapping("/{cartId}")
    public ResponseEntity<String> removeFromCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long cartId) {
        log.info("Remove from cart: userId={} cartId={}", userId, cartId);
        cartService.removeFromCart(userId, cartId);
        return ResponseEntity.ok("Cart item removed.");
    }

    /** DELETE — Clear entire cart. */
    @DeleteMapping
    public ResponseEntity<String> clearCart(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("Clear cart: userId={}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.ok("Cart cleared.");
    }
}
