package com.cts.orderservice;

import com.cts.orderservice.client.CatalogClient;
import com.cts.orderservice.client.InventoryClient;
import com.cts.orderservice.dto.response.AvailabilityDto;
import com.cts.orderservice.dto.response.BookResponse;
import com.cts.orderservice.dto.response.OrderResponse;
import com.cts.orderservice.exception.feignclientexception.InventoryServiceDownException;
import com.cts.orderservice.exception.inventory.InsufficientStockException;
import com.cts.orderservice.exception.order.InvalidOrderException;
import com.cts.orderservice.exception.order.InvalidOrderStatusException;
import com.cts.orderservice.exception.order.OrderNotFoundException;
import com.cts.orderservice.model.Cart;
import com.cts.orderservice.model.OrderItem;
import com.cts.orderservice.model.Orders;
import com.cts.orderservice.repository.CartRepository;
import com.cts.orderservice.repository.OrderRepository;
import com.cts.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OrderServiceImpl}.
 * Verifies order business logic with mocked repositories and clients.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Cart cart;
    private Orders order;
    private BookResponse book;
    private AvailabilityDto availability;

    /**
     * Initializes sample cart, order, book, and availability data before each test.
     */
    @BeforeEach
    void setUp() {
        book = new BookResponse(100L, "Clean Code", new BigDecimal("299.99"), "Robert Martin", "Programming");

        availability = new AvailabilityDto();
        availability.setBookId(100L);
        availability.setAvailableQuantity(50);
        availability.setInStock(true);

        cart = new Cart();
        cart.setCartId(1L);
        cart.setUserId(10L);
        cart.setBookId(100L);
        cart.setBookTitle("Clean Code");
        cart.setQuantity(2);

        OrderItem item = new OrderItem();
        item.setOrderItemId(1L);
        item.setBookId(100L);
        item.setBookTitle("Clean Code");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("299.99"));

        order = new Orders();
        order.setOrderId(1L);
        order.setUserId(10L);
        order.setStatus("PLACED");
        order.setTotalAmount(new BigDecimal("599.98"));
        order.setOrderItems(new ArrayList<>(List.of(item)));
        item.setOrder(order);
    }

    // ================== PLACE ORDER ==================

    /**
     * Tests successful order placement with valid cart and available stock.
     */
    @Test
    void test_placeOrder_success() {
        when(cartRepository.findByUserId(10L)).thenReturn(List.of(cart));
        when(catalogClient.getBookById(100L)).thenReturn(book);
        when(inventoryClient.checkAvailability(100L)).thenReturn(availability);
        when(orderRepository.save(any())).thenReturn(order);

        OrderResponse response = orderService.placeOrder(10L);

        assertNotNull(response);
        assertEquals("PLACED", response.getStatus());
        verify(cartRepository).deleteByUserId(10L);
    }

    /**
     * Tests InvalidOrderException when cart is empty.
     */
    @Test
    void test_placeOrder_emptyCart() {
        when(cartRepository.findByUserId(10L)).thenReturn(List.of());

        assertThrows(InvalidOrderException.class,
                () -> orderService.placeOrder(10L));
    }

    /**
     * Tests InsufficientStockException when available quantity is less than requested.
     */
    @Test
    void test_placeOrder_insufficientStock() {
        availability.setAvailableQuantity(1);
        when(cartRepository.findByUserId(10L)).thenReturn(List.of(cart));
        when(catalogClient.getBookById(100L)).thenReturn(book);
        when(inventoryClient.checkAvailability(100L)).thenReturn(availability);

        assertThrows(InsufficientStockException.class,
                () -> orderService.placeOrder(10L));
    }

    /**
     * Tests InsufficientStockException when book is out of stock.
     */
    @Test
    void test_placeOrder_outOfStock() {
        availability.setInStock(false);
        when(cartRepository.findByUserId(10L)).thenReturn(List.of(cart));
        when(catalogClient.getBookById(100L)).thenReturn(book);
        when(inventoryClient.checkAvailability(100L)).thenReturn(availability);

        assertThrows(InsufficientStockException.class,
                () -> orderService.placeOrder(10L));
    }

    /**
     * Tests InventoryServiceDownException when stock reduction fails.
     */
    @Test
    void test_placeOrder_stockReduceFails() {
        when(cartRepository.findByUserId(10L)).thenReturn(List.of(cart));
        when(catalogClient.getBookById(100L)).thenReturn(book);
        when(inventoryClient.checkAvailability(100L)).thenReturn(availability);
        when(orderRepository.save(any())).thenReturn(order);
        doThrow(new RuntimeException("Connection error")).when(inventoryClient).reduce(any());

        assertThrows(InventoryServiceDownException.class,
                () -> orderService.placeOrder(10L));
    }

    // ================== GET ORDER BY ID ==================

    /**
     * Tests successful retrieval of an order by ID.
     */
    @Test
    void test_getOrderById_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getOrderId());
    }

    /**
     * Tests OrderNotFoundException when order does not exist.
     */
    @Test
    void test_getOrderById_notFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrderById(99L));
    }

    // ================== GET ORDERS BY USER ==================

    /**
     * Tests successful retrieval of all orders for a user.
     */
    @Test
    void test_getOrdersByUserId_success() {
        when(orderRepository.findByUserId(10L)).thenReturn(List.of(order));

        List<OrderResponse> result = orderService.getOrdersByUserId(10L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getUserId());
    }

    // ================== GET ALL ORDERS ==================

    /**
     * Tests successful retrieval of all orders.
     */
    @Test
    void test_getAllOrders_success() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderResponse> result = orderService.getAllOrders();

        assertEquals(1, result.size());
    }

    // ================== UPDATE STATUS ==================

    /**
     * Tests successful status update to PLACED.
     */
    @Test
    void test_updateOrderStatus_placed() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        orderService.updateOrderStatus(1L, "PLACED");
        verify(orderRepository).save(any());
    }

    /**
     * Tests successful status update to SHIPPED.
     */
    @Test
    void test_updateOrderStatus_shipped() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        orderService.updateOrderStatus(1L, "SHIPPED");
        verify(orderRepository).save(any());
    }

    /**
     * Tests successful status update to CANCELLED.
     */
    @Test
    void test_updateOrderStatus_cancelled() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        orderService.updateOrderStatus(1L, "CANCELLED");
        verify(orderRepository).save(any());
    }

    /**
     * Tests InvalidOrderStatusException when an unrecognised status is provided.
     */
    @Test
    void test_updateOrderStatus_invalid() {
        assertThrows(InvalidOrderStatusException.class,
                () -> orderService.updateOrderStatus(1L, "PROCESSING"));
    }

    // ================== CANCEL ORDER ==================

    /**
     * Tests successful cancellation of a PENDING order.
     */
    @Test
    void test_cancelOrder_pending_success() {
        order.setStatus("PENDING");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1L);

        assertEquals("CANCELLED", order.getStatus());
        verify(orderRepository).save(order);
    }

    /**
     * Tests successful cancellation of a PLACED order.
     */
    @Test
    void test_cancelOrder_placed_success() {
        order.setStatus("PLACED");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1L);

        assertEquals("CANCELLED", order.getStatus());
    }

    /**
     * Tests InvalidOrderException when cancelling a SHIPPED order.
     */
    @Test
    void test_cancelOrder_shipped() {
        order.setStatus("SHIPPED");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderException.class,
                () -> orderService.cancelOrder(1L));
    }

    /**
     * Tests InvalidOrderException when cancelling a DELIVERED order.
     */
    @Test
    void test_cancelOrder_delivered() {
        order.setStatus("DELIVERED");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderException.class,
                () -> orderService.cancelOrder(1L));
    }

    /**
     * Tests InvalidOrderException when order is already cancelled.
     */
    @Test
    void test_cancelOrder_alreadyCancelled() {
        order.setStatus("CANCELLED");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderException.class,
                () -> orderService.cancelOrder(1L));
    }

    /**
     * Tests that a release failure is silently logged without rethrowing.
     */
    @Test
    void test_cancelOrder_releaseFails_logsOnly() {
        order.setStatus("PLACED");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doThrow(new RuntimeException("Inventory down")).when(inventoryClient).release(any());

        assertDoesNotThrow(() -> orderService.cancelOrder(1L));
        assertEquals("CANCELLED", order.getStatus());
    }
}