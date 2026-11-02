package com.cts.orderservice;

import com.cts.orderservice.model.Cart;
import com.cts.orderservice.model.OrderItem;
import com.cts.orderservice.model.Orders;
import com.cts.orderservice.repository.CartRepository;
import com.cts.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JPA integration tests for Cart and Order repositories.
 * Seeds real data into MySQL using @DataJpaTest with rollback disabled.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderServiceJpaTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Seeds cart entries for 30 books and one order for userId=1.
     * Skips existing entries to avoid duplicate key violations.
     */
    @Test
    @Rollback(value = false)
    @DisplayName("Seed Cart and Orders for all 30 catalog books")
    void seedCartAndOrders_success() {
        long[] bookIds = {
                1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L,
                11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L,
                21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L
        };

        long userId = 1L;
        Random random = new Random();
        List<Cart> cartRecords = new ArrayList<>();

        for (long bookId : bookIds) {
            if (cartRepository.findByUserIdAndBookId(userId, bookId).isPresent()) {
                System.out.println("Cart entry already exists for bookId: " + bookId + ". Skipping.");
                continue;
            }
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setBookId(bookId);
            cart.setBookTitle("Book " + bookId);
            cart.setQuantity(1 + random.nextInt(5));
            cartRecords.add(cart);
        }

        if (!cartRecords.isEmpty()) {
            cartRepository.saveAll(cartRecords);
        }

        long totalCart = cartRepository.count();
        assertThat(totalCart).isGreaterThanOrEqualTo(30);
        System.out.println("Cart seeded successfully. Total records: " + totalCart);

        if (orderRepository.findByUserId(userId).isEmpty()) {
            Orders order = new Orders();
            order.setUserId(userId);
            order.setStatus("PLACED");
            order.setTotalAmount(new BigDecimal("999.99"));

            OrderItem item = new OrderItem();
            item.setBookId(1L);
            item.setBookTitle("Book 1");
            item.setQuantity(2);
            item.setUnitPrice(new BigDecimal("499.99"));
            item.setOrder(order);

            order.setOrderItems(List.of(item));
            orderRepository.save(order);
        }

        long totalOrders = orderRepository.count();
        assertThat(totalOrders).isGreaterThanOrEqualTo(1);
        System.out.println("Orders seeded successfully. Total records: " + totalOrders);
    }
}