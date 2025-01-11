package org.pancakelab.service;

import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;

import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class PreparationTaskTest {
    private static final ConcurrentMap<UUID, OrderInfo> orders = new ConcurrentHashMap<>();
    private static final BlockingDeque<UUID> deliveryQueue = new LinkedBlockingDeque<>();

    @Test
    public void givenInvalidStatusOfOrder_whenProcessed_thenWarningShouldBeLogged() {
        // Given
        var order = new OrderDetails.Builder()
                .addPancake(mock(Pancake.class))
                .withDeliveryInfo(mock(DeliveryInfo.class))
                .build();
        orders.put(order.getOrderId(), new OrderInfo(order, ORDER_STATUS.DELIVERED));
        // When
        final ORDER_STATUS status = new PreparationTask(deliveryQueue, orders, order.getOrderId()).call();
        // Then
        assertSame(status, ORDER_STATUS.INVALID);
        assertFalse(deliveryQueue.contains(order.getOrderId()));
    }

    @Test
    public void givenOrderNotFoundInTheDatabase_whenProcessed_thenWarningShouldBeLogged() {
        // Given
        var orderId = UUID.randomUUID();
        // When
        final ORDER_STATUS status = new PreparationTask(deliveryQueue, orders, orderId).call();
        // Then
        assertSame(status, ORDER_STATUS.NOT_FOUND);
        assertFalse(deliveryQueue.contains(orderId));
    }

    @Test
    public void givenOrderIsReadyForDelivery_whenProcessed_thenOrderShouldBeAddedToDeliveryQueue() {
        // Given
        var order = new OrderDetails.Builder()
                .addPancake(mock(Pancake.class))
                .withDeliveryInfo(mock(DeliveryInfo.class))
                .build();
        orders.put(order.getOrderId(), new OrderInfo(order, ORDER_STATUS.PENDING));
        // When
        final ORDER_STATUS status = new PreparationTask(deliveryQueue, orders, order.getOrderId()).call();
        // Then
        assertSame(status, ORDER_STATUS.READY_FOR_DELIVERY);
        assertTrue(deliveryQueue.contains(order.getOrderId()));
    }
}