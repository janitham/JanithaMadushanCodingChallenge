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
import static org.pancakelab.service.OrderServiceImpl.*;

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
    public void givenValidOrderDetails_whenOpenOrder_thenSuccessful() {
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
    public void givenNoOrderDetails_whenOpen_thenThrowException() {
        // Given
        // When
        // Then
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.open(null),
                ORDER_DETAILS_SHOULD_NOT_BE_NULL
        );
    }

    @Test
    public void givenExistingOrder_whenOpen_thenThrowException() {
        // Given
        var orderDetails = new OrderDetails.Builder()
                .withDeliveryInfo(mock(DeliveryInfo.class))
                .addPancake(mock(Pancake.class))
                .build();
        orders.put(orderDetails.getOrderId(), new OrderInfo(orderDetails, ORDER_STATUS.PENDING));
        // When
        // Then
        assertThrows(
                IllegalArgumentException.class, () -> orderService.open(orderDetails),
                ORDER_CANNOT_BE_OPENED_WITH_THE_SAME_ORDER_ID
        );
    }

    @Test
    public void givenNullOrderId_whenComplete_thenThrowException() {
        // Given
        // When
        // Then
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.complete(null), ORDER_DETAILS_SHOULD_NOT_BE_NULL
        );
    }

    @Test
    public void givenNonExistingOrderId_whenComplete_thenThrowException() {
        // Given
        // When
        // Then
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.complete(UUID.randomUUID()), ORDER_NOT_FOUND
        );
    }

    @Test
    public void givenValidOrder_whenComplete_thenOrderShouldBeCompleted() {
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