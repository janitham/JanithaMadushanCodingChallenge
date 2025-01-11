package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OrderServiceTest {

    private ConcurrentMap<UUID, OrderInfo> orders;
    private KitchenService kitchenService;
    private OrderService orderService;

    @BeforeEach
    public void setUp() {
        orders = new ConcurrentHashMap<>();
        kitchenService = mock(KitchenService.class);
        orderService = new OrderServiceImpl(kitchenService, orders);
    }

    @Test
    public void When_OrderServiceIsCreated_Then_NoExceptions() {
        // Given
        var orderDetails = new OrderDetails.Builder()
                .withDeliveryInfo(mock(DeliveryInfo.class))
                .addPancake(mock(Pancake.class))
                .build();
        // When
        var uuid = orderService.open(orderDetails);
        // Then
        assertNotNull(uuid);
    }

    @Test
    public void When_OrderDetails_are_not_provided_Then_throw_Exception() {
        // Given
        // When
        // Then
        assertThrows(IllegalArgumentException.class, () -> orderService.open(null));
    }

    @Test
    public void When_order_is_created_with_exiting_then_throw_Exception() {
        // Given
        var orderDetails = new OrderDetails.Builder()
                .withDeliveryInfo(mock(DeliveryInfo.class))
                .addPancake(mock(Pancake.class))
                .build();
        orders.put(orderDetails.getOrderId(), new OrderInfo(orderDetails, ORDER_STATUS.PENDING));
        // When
        // Then
        assertThrows(IllegalArgumentException.class, () -> orderService.open(orderDetails));
    }

    @Test
    public void When_completing_order_with_null_orderId_then_throw_Exception() {
        // Given
        // When
        // Then
        assertThrows(IllegalArgumentException.class, () -> orderService.complete(null));
    }

    @Test
    public void When_completing_order_with_non_existing_orderId_then_throw_Exception() {
        // Given
        // When
        // Then
        assertThrows(IllegalArgumentException.class, () -> orderService.complete(UUID.randomUUID()));
    }

    @Test
    public void When_completing_order_Then_it_should_be_completed() {
        // Given
        var orderDetails = new OrderDetails.Builder()
                .withDeliveryInfo(mock(DeliveryInfo.class))
                .addPancake(mock(Pancake.class))
                .build();
        orders.put(orderDetails.getOrderId(), new OrderInfo(orderDetails, ORDER_STATUS.PENDING));
        // When
        orderService.complete(orderDetails.getOrderId());
        // Then
        verify(kitchenService).processOrder(orderDetails.getOrderId());
    }
}