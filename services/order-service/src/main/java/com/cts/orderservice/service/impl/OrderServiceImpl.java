package com.cts.orderservice.service.impl;

import com.cts.orderservice.client.CatalogClient;
import com.cts.orderservice.client.InventoryClient;
import com.cts.orderservice.dto.request.ReserveRequest;
import com.cts.orderservice.dto.response.*;
import com.cts.orderservice.exception.inventory.InsufficientStockException;
import com.cts.orderservice.exception.feignclientexception.CatalogServiceDownException;
import com.cts.orderservice.exception.feignclientexception.InventoryServiceDownException;
import com.cts.orderservice.exception.order.*;
import com.cts.orderservice.model.*;
import com.cts.orderservice.repository.CartRepository;
import com.cts.orderservice.repository.OrderRepository;
import com.cts.orderservice.service.OrderService;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service implementation for order operations.
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            CatalogClient catalogClient,
            InventoryClient inventoryClient) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
    }

    /**
     * Places an order by reading cart, validating stock, saving order and clearing cart.
     * @param userId the ID of the user placing the order
     * @return OrderResponse with order details and status PLACED
     * @throws InvalidOrderException if cart is empty
     * @throws InsufficientStockException if stock is unavailable for any item
     * @throws InventoryServiceDownException if stock reduction fails
     */
    @Override
    @Transactional
    public OrderResponse placeOrder(Long userId) {

        // Step 1: Validate and get cart items
        List<Cart> cartItems = validateAndGetCart(userId);

        // Step 2: Process each cart item
        List<OrderItem> items = processCartItems(cartItems);

        // Step 3: Save order as PENDING — not confirmed yet
        Orders saved = saveOrder(userId, items);
        log.info("Order saved as PENDING. OrderId={}", saved.getOrderId());

        // Step 4: Reduce stock — throws on failure, rolls back Step 3
        reduceStockForItems(saved.getOrderItems(), saved.getOrderId());

        // Step 5: All stock reduced — promote to PLACED
        saved.setStatus("PLACED");
        orderRepository.save(saved);
        log.info("Order confirmed as PLACED. OrderId={}", saved.getOrderId());

        // Step 6: Clear cart only after order is fully confirmed
        cartRepository.deleteByUserId(userId);
        log.info("Cart cleared: userId={}", userId);

        return toResponse(saved);
    }

    // ---- PLACE ORDER HELPERS ----

    /**
     * Validates and returns cart items for the user.
     * @param userId the ID of the user
     * @return list of Cart items
     * @throws InvalidOrderException if cart is empty
     */
    private List<Cart> validateAndGetCart(Long userId) {
        List<Cart> cartItems =
                cartRepository.findByUserId(userId);
        if (cartItems == null || cartItems.isEmpty())
            throw new InvalidOrderException("Cart is empty. Add items before ordering.");
        return cartItems;
    }

    /**
     * Processes cart items by fetching book details and validating stock.
     * @param cartItems list of cart items to process
     * @return list of OrderItem built from cart
     */
    private List<OrderItem> processCartItems(List<Cart> cartItems) {
        List<OrderItem> items = new ArrayList<>();
        for (Cart cartItem : cartItems) {
            BookResponse book = getBookWithResilience(
                    cartItem.getBookId()).join();
            AvailabilityDto availability = checkStock(
                    cartItem.getBookId()).join();
            validateStock(availability, cartItem);
            items.add(buildOrderItem(cartItem, book));
        }
        return items;
    }

    /**
     * Validates stock availability for a cart item.
     * @param availability the availability data from inventory service
     * @param cartItem the cart item to validate against
     * @throws InsufficientStockException if stock is insufficient
     */
    private void validateStock(AvailabilityDto availability, Cart cartItem) {
        if (!availability.isInStock() ||
                availability.getAvailableQuantity()
                        < cartItem.getQuantity())
            throw new InsufficientStockException("Insufficient stock for bookId: " + cartItem.getBookId());
    }

    /**
     * Builds an OrderItem from cart and book data.
     * @param cartItem the cart item
     * @param book the book details from catalog
     * @return constructed OrderItem
     */
    private OrderItem buildOrderItem(Cart cartItem, BookResponse book) {
        OrderItem item = new OrderItem();
        item.setBookId(cartItem.getBookId());
        item.setBookTitle(book.getTitle());
        item.setQuantity(cartItem.getQuantity());
        item.setUnitPrice(book.getPrice());
        return item;
    }

    /**
     * Calculates total order amount from all items.
     * @param items list of order items
     * @return total amount as BigDecimal
     */
    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(i -> i.getUnitPrice()
                        .multiply(BigDecimal.valueOf(
                                i.getQuantity())))
                .reduce(BigDecimal.ZERO,
                        BigDecimal::add);
    }

    /**
     * Saves order with PENDING status before stock reduction.
     * @param userId the ID of the user
     * @param items list of order items
     * @return saved Orders entity
     */
    private Orders saveOrder(Long userId, List<OrderItem> items) {
        Orders order = new Orders();
        order.setUserId(userId);
        order.setStatus("PENDING");
        order.setTotalAmount(calculateTotal(items));
        items.forEach(item -> item.setOrder(order));
        order.setOrderItems(items);
        Orders saved = orderRepository.save(order);
        log.info("Order saved. OrderID: {}", saved.getOrderId());
        return saved;
    }

    /**
     * Reduces stock for all order items. Throws on failure to trigger rollback.
     * @param items list of order items to reduce stock for
     * @param orderId the ID of the order
     * @throws InventoryServiceDownException if stock reduction fails for any item
     */
    private void reduceStockForItems(List<OrderItem> items, Long orderId) {
        for (OrderItem item : items) {
            try {
                reduceStockSafe(new ReserveRequest(
                        item.getBookId(),
                        item.getQuantity()));
                log.info("Stock reduced. orderId={} bookId={} qty={}",
                        orderId, item.getBookId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Stock reduce FAILED — rolling back order. orderId={} bookId={}",
                        orderId, item.getBookId());
                throw new InventoryServiceDownException(
                        "Failed to reduce stock for bookId: " + item.getBookId()
                                + ". Order has been rolled back.");
            }
        }
    }

    // ============================================================
    //  READ OPERATIONS
    // ============================================================

    /**
     * Retrieves an order by its ID.
     * @param orderId the ID of the order
     * @return OrderResponse for the given order
     * @throws OrderNotFoundException if order does not exist
     */
    @Override
    @Transactional
    public OrderResponse getOrderById(Long orderId) {
        return toResponse(findOrThrow(orderId));
    }

    /**
     * Returns all orders for a given user.
     * @param userId the ID of the user
     * @return list of OrderResponse for the user
     */
    @Override
    @Transactional
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns all orders in the system.
     * @return list of all OrderResponse
     */
    @Override
    @Transactional
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ============================================================
    //  UPDATE OPERATIONS
    // ============================================================

    /**
     * Updates the status of an order.
     * @param orderId the ID of the order
     * @param status the new status value
     * @throws InvalidOrderStatusException if status is not a valid value
     * @throws OrderNotFoundException if order does not exist
     */
    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        if (!List.of("PLACED", "SHIPPED", "DELIVERED", "CANCELLED")
                .contains(status))
            throw new InvalidOrderStatusException(
                    "Invalid status: " + status
                            + ". Must be PLACED, SHIPPED,"
                            + " DELIVERED or CANCELLED");
        Orders order = findOrThrow(orderId);
        order.setStatus(status);
        orderRepository.save(order);
        log.info("Status updated: orderId={} status={}",
                orderId, status);
    }

    /**
     * Cancels an order and attempts to release stock.
     * @param orderId the ID of the order to cancel
     * @throws InvalidOrderException if order is SHIPPED, DELIVERED or already CANCELLED
     * @throws OrderNotFoundException if order does not exist
     */
    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Orders order = findOrThrow(orderId);
        if ("SHIPPED".equals(order.getStatus()))
            throw new InvalidOrderException("Cannot cancel a SHIPPED order");
        if ("DELIVERED".equals(order.getStatus()))
            throw new InvalidOrderException("Cannot cancel a DELIVERED order");
        if ("CANCELLED".equals(order.getStatus()))
            throw new InvalidOrderException("Order is already cancelled");
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        for (OrderItem item : order.getOrderItems()) {
            try {
                releaseStockSafe(new ReserveRequest(
                        item.getBookId(),
                        item.getQuantity()));
            } catch (Exception e) {
                log.error("Release failed bookId={}",
                        item.getBookId());
            }
        }
        log.info("Order cancelled. ID: {}", orderId);
    }

    // ============================================================
    //  RESILIENCE METHODS
    // ============================================================

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

    /**
     * Checks stock availability from inventory service with retry and time limiter.
     * @param bookId the ID of the book to check
     * @return CompletableFuture containing AvailabilityDto
     * @throws InventoryServiceDownException if inventory service is unavailable after retries
     */
    @Retry(name = "inventoryService", fallbackMethod = "inventoryFallback")
    @TimeLimiter(name = "inventoryService")
    public CompletableFuture<AvailabilityDto> checkStock(Long bookId) {
        return CompletableFuture.supplyAsync(() ->
                inventoryClient.checkAvailability(bookId));
    }

    /**
     * Reduces stock in inventory service with retry.
     * @param req the reserve request containing bookId and quantity
     * @throws InventoryServiceDownException if inventory service is unavailable after retries
     */
    @Retry(name = "inventoryService", fallbackMethod = "reduceFallback")
    public void reduceStockSafe(ReserveRequest req) {
        inventoryClient.reduce(req);
    }

    /**
     * Releases stock in inventory service with retry.
     * @param req the reserve request containing bookId and quantity
     * @throws InventoryServiceDownException if inventory service is unavailable after retries
     */
    @Retry(name = "inventoryService", fallbackMethod = "releaseFallback")
    public void releaseStockSafe(ReserveRequest req) {
        inventoryClient.release(req);
    }

    // ============================================================
    //  FALLBACK METHODS
    // ============================================================

    /**
     * Fallback triggered when catalog service is unavailable.
     * @param bookId the ID of the book that failed to fetch
     * @param ex the exception that triggered the fallback
     * @throws CatalogServiceDownException always
     */
    public CompletableFuture<BookResponse> catalogFallback(Long bookId, Throwable ex) {
        log.error("Catalog unavailable bookId={}", bookId);
        throw new CatalogServiceDownException(
                "Catalog service unavailable");
    }

    /**
     * Fallback triggered when inventory service is unavailable during stock check.
     * @param bookId the ID of the book that failed
     * @param ex the exception that triggered the fallback
     * @throws InventoryServiceDownException always
     */
    public CompletableFuture<AvailabilityDto> inventoryFallback(Long bookId, Throwable ex) {
        log.error("Inventory unavailable bookId={}", bookId);
        throw new InventoryServiceDownException(
                "Inventory service unavailable");
    }

    /**
     * Fallback triggered when stock reduction fails after retries.
     * @param req the reserve request that failed
     * @param ex the exception that triggered the fallback
     * @throws InventoryServiceDownException always
     */
    public void reduceFallback(ReserveRequest req, Throwable ex) {
        log.error("Reduce failed bookId={}", req.getBookId());
        throw new InventoryServiceDownException(
                "Inventory service down");
    }

    /**
     * Fallback triggered when stock release fails after retries.
     * @param req the reserve request that failed
     * @param ex the exception that triggered the fallback
     * @throws InventoryServiceDownException always
     */
    public void releaseFallback(ReserveRequest req, Throwable ex) {
        log.error("CRITICAL: Release failed bookId={}", req.getBookId());
        throw new InventoryServiceDownException(
                "Inventory service down. Could not release stock for bookId: "
                        + req.getBookId());
    }

    // ============================================================
    //  COMMON HELPERS
    // ============================================================

    /**
     * Finds an order by ID or throws OrderNotFoundException.
     * @param orderId the ID of the order to find
     * @return Orders entity
     * @throws OrderNotFoundException if order does not exist
     */
    private Orders findOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found: " + orderId));
    }

    /**
     * Converts Orders entity to OrderResponse DTO.
     * @param o the Orders entity to convert
     * @return OrderResponse DTO
     */
    private OrderResponse toResponse(Orders o) {
        OrderResponse res = new OrderResponse();
        res.setOrderId(o.getOrderId());
        res.setUserId(o.getUserId());
        res.setOrderDate(o.getOrderDate());
        res.setTotalAmount(o.getTotalAmount());
        res.setStatus(o.getStatus());
        if (o.getOrderItems() != null) {
            res.setItems(o.getOrderItems().stream()
                    .map(i -> {
                        OrderResponse.OrderItemResponse ir =
                                new OrderResponse
                                        .OrderItemResponse();
                        ir.setOrderItemId(i.getOrderItemId());
                        ir.setBookId(i.getBookId());
                        ir.setBookTitle(i.getBookTitle());
                        ir.setQuantity(i.getQuantity());
                        ir.setUnitPrice(i.getUnitPrice());
                        return ir;
                    })
                    .collect(Collectors.toList()));
        }
        return res;
    }
}
