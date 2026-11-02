package com.cts.orderservice.service;

import com.cts.orderservice.dto.response.OrderResponse;
import java.util.List;

/**
 * Service interface for order operations.
 */
public interface OrderService {

    /** Places order by reading cart from DB. */
    OrderResponse placeOrder(Long userId);

    /** Retrieves order by ID. */
    OrderResponse getOrderById(Long orderId);

    /** Returns all orders for a user. */
    List<OrderResponse> getOrdersByUserId(Long userId);

    /** Returns all orders — admin only. */
    List<OrderResponse> getAllOrders();

    /** Updates order status — admin only. */
    void updateOrderStatus(Long orderId, String status);

    /** Cancels an order. */
    void cancelOrder(Long orderId);
}