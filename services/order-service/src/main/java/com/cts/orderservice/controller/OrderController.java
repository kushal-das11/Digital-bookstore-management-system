package com.cts.orderservice.controller;

import com.cts.orderservice.dto.response.OrderResponse;
import com.cts.orderservice.service.OrderService;
import com.cts.orderservice.dto.request.StatusRequest;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for order operations.
 */
@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** POST — Place order from cart. */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("Place order: userId={}", userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(userId));
    }

    /** GET — Get order by ID. */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long orderId) {
        log.info("Get order: orderId={}", orderId);
        return ResponseEntity.ok(
                orderService.getOrderById(orderId));
    }

    /** GET — Get all orders of a user. */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(
            @PathVariable Long userId) {
        log.info("Get orders: userId={}", userId);
        return ResponseEntity.ok(
                orderService.getOrdersByUserId(userId));
    }

    /** GET — Get order items. */
    @GetMapping("/{orderId}/items")
    public ResponseEntity<OrderResponse> getOrderItems(
            @PathVariable Long orderId) {
        log.info("Get items: orderId={}", orderId);
        return ResponseEntity.ok(
                orderService.getOrderById(orderId));
    }

    /** GET — Get order status. */
    @GetMapping("/{orderId}/status")
    public ResponseEntity<String> getStatus(
            @PathVariable Long orderId) {
        log.info("Get status: orderId={}", orderId);
        OrderResponse order =
                orderService.getOrderById(orderId);
        return ResponseEntity.ok(
                "Order #" + orderId
                        + " Status: " + order.getStatus());
    }

    /** PATCH — Update status. ADMIN only. */
    @PatchMapping("/status")
    public ResponseEntity<String> updateStatus(
            @Valid @RequestBody StatusRequest request) {
        log.info("Update status: orderId={} status={}",
                request.getOrderId(), request.getStatus());
        orderService.updateOrderStatus(
                request.getOrderId(), request.getStatus());
        return ResponseEntity.ok(
                "Status updated to " + request.getStatus());
    }

    /** PATCH — Cancel order. */
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(
            @PathVariable Long orderId) {
        log.info("Cancel order: orderId={}", orderId);
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok(
                "Order #" + orderId + " cancelled.");
    }

    /** GET — All orders. ADMIN only. */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("Get all orders");
        return ResponseEntity.ok(
                orderService.getAllOrders());
    }
}
