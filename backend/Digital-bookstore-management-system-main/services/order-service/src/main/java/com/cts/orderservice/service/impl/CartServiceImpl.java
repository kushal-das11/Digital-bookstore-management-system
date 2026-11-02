package com.cts.orderservice.service.impl;

import com.cts.orderservice.client.CatalogClient;
import com.cts.orderservice.dto.request.CartRequest;
import com.cts.orderservice.dto.response.BookResponse;
import com.cts.orderservice.dto.response.CartResponse;
import com.cts.orderservice.exception.order.CartItemNotFoundException;
import com.cts.orderservice.exception.feignclientexception.CatalogServiceDownException;
import com.cts.orderservice.model.Cart;
import com.cts.orderservice.repository.CartRepository;
import com.cts.orderservice.service.CartService;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service implementation for cart operations.
 */
@Service
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CatalogClient  catalogClient;

    public CartServiceImpl(CartRepository cartRepository, CatalogClient catalogClient) {
        this.cartRepository = cartRepository;
        this.catalogClient  = catalogClient;
    }

    /**
     * Adds a book to the user's cart. Increments quantity if already present.
     * @param userId the ID of the user
     * @param request the cart request containing bookId and quantity
     * @return CartResponse with updated cart details
     * @throws CatalogServiceDownException if catalog service is unavailable
     */
    @Override
    @Transactional
    public CartResponse addToCart(Long userId, CartRequest request) {
        BookResponse book = getBookWithResilience(
                request.getBookId()).join();

        Optional<Cart> optionalCart =
                cartRepository.findByUserIdAndBookId(userId, book.getBookId());

        if (optionalCart.isPresent()) {
            Cart existing = optionalCart.get();
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            log.info("Updated cart: userId={} bookId={}", userId, book.getBookId());
            return toResponse(cartRepository.save(existing));
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setBookId(book.getBookId());
        cart.setBookTitle(book.getTitle());
        cart.setQuantity(request.getQuantity());
        log.info("Added to cart: userId={} bookId={}", userId, book.getBookId());
        return toResponse(cartRepository.save(cart));
    }

    /**
     * Returns all cart items for the given user.
     * @param userId the ID of the user
     * @return list of CartResponse for the user
     */
    @Override
    public List<CartResponse> getCart(Long userId) {
        log.debug("Fetching cart: userId={}", userId);
        return cartRepository.findByUserId(userId)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates quantity of an existing cart item. Verifies user ownership.
     * @param userId the ID of the user
     * @param cartId the ID of the cart item
     * @param request the cart request containing new quantity
     * @return CartResponse with updated quantity
     * @throws CartItemNotFoundException if item not found or does not belong to user
     */
    @Override
    @Transactional
    public CartResponse updateCartItem(Long userId, Long cartId, CartRequest request) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() ->
                        new CartItemNotFoundException("Cart item not found: " + cartId));
        if (!cart.getUserId().equals(userId))
            throw new CartItemNotFoundException("Cart item does not belong to this user");
        cart.setQuantity(request.getQuantity());
        log.info("Cart updated: cartId={} qty={}", cartId, request.getQuantity());
        return toResponse(cartRepository.save(cart));
    }

    /**
     * Removes a specific cart item. Verifies user ownership before deletion.
     * @param userId the ID of the user
     * @param cartId the ID of the cart item to remove
     * @throws CartItemNotFoundException if item not found or does not belong to user
     */
    @Override
    @Transactional
    public void removeFromCart(Long userId, Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() ->
                        new CartItemNotFoundException("Cart item not found: " + cartId));
        if (!cart.getUserId().equals(userId))
            throw new CartItemNotFoundException("Cart item does not belong to this user");
        cartRepository.deleteById(cartId);
        log.info("Removed from cart: cartId={}", cartId);
    }

    /**
     * Clears all cart items for the given user.
     * @param userId the ID of the user
     */
    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
        log.info("Cart cleared: userId={}", userId);
    }

    // ---- RESILIENCE ----

    /**
     * Fetches book from catalog service with retry and time limiter.
     * @param bookId the ID of the book to fetch
     * @return CompletableFuture containing BookResponse
     * @throws CatalogServiceDownException if catalog service is unavailable after retries
     */
    @Retry(name = "catalogService", fallbackMethod = "catalogFallback")
    @TimeLimiter(name = "catalogService")
    public CompletableFuture<BookResponse> getBookWithResilience(Long bookId) {
        return CompletableFuture.supplyAsync(() ->
                catalogClient.getBookById(bookId));
    }

    // ---- FALLBACK ----

    /**
     * Fallback triggered when catalog service is unavailable.
     * @param bookId the ID of the book that failed to fetch
     * @param ex the exception that triggered the fallback
     * @throws CatalogServiceDownException always
     */
    public CompletableFuture<BookResponse> catalogFallback(Long bookId, Throwable ex) {
        log.error("Catalog unavailable bookId={}", bookId);
        throw new CatalogServiceDownException("Catalog service unavailable");
    }

    // ---- HELPER ----

    /**
     * Converts Cart entity to CartResponse DTO.
     * @param cart the Cart entity to convert
     * @return CartResponse DTO
     */
    private CartResponse toResponse(Cart cart) {
        return new CartResponse(
                cart.getCartId(),
                cart.getUserId(),
                cart.getBookId(),
                cart.getBookTitle(),
                cart.getQuantity(),
                cart.getAddedAt()
        );
    }
}