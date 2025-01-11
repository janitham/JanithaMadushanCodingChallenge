package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;
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
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.open(null)
        );
        assertEquals(ORDER_DETAILS_SHOULD_NOT_BE_NULL, exception.getMessage());
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
        Exception exception = assertThrows(
                IllegalStateException.class, () -> orderService.open(orderDetails)
        );
        assertEquals(ORDER_CANNOT_BE_OPENED_WITH_THE_SAME_ORDER_ID, exception.getMessage());
    }

    @Test
    public void givenNullOrderId_whenComplete_thenThrowException() {
        // Given
        // When
        // Then
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.complete(null)
        );
        assertEquals(ORDER_CANNNOT_BE_PROCESSED_WITHOUT_ORDER_ID, exception.getMessage());
    }

    @Test
    public void givenNonExistingOrderId_whenComplete_thenThrowException() {
        // Given
        // When
        // Then
        Exception exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.complete(UUID.randomUUID())
        );
        assertEquals(ORDER_NOT_FOUND, exception.getMessage());
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