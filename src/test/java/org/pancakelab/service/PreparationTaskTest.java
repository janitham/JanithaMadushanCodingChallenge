package org.pancakelab.service;

import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class PreparationTaskTest {
    private static final ConcurrentMap<UUID, OrderDetails> orders = new ConcurrentHashMap<>();
    private static final BlockingDeque<UUID> deliveryQueue = new LinkedBlockingDeque<>();
    private static final ConcurrentHashMap<UUID, OrderStatus> orderStatus = new ConcurrentHashMap<>();

    @Test
    public void givenOrderNotFoundInTheDatabase_whenProcessed_thenWarningShouldBeLogged() {
        // Given
        final UUID orderId = UUID.randomUUID();
        // When
        new PreparationTask(deliveryQueue, orders, orderId, orderStatus).run();
        // Then
        assertSame(OrderStatus.ERROR, orderStatus.get(orderId));
        assertFalse(deliveryQueue.contains(orderId));
    }

    @Test
    public void givenOrderIsReadyForDelivery_whenProcessed_thenOrderShouldBeAddedToDeliveryQueue() {
        // Given
        OrderDetails order = new OrderDetails.Builder()
                .withPanCakes(Map.of(PancakeMenu.DARK_CHOCOLATE_PANCAKE, 2))
                .withDeliveryInfo(mock(DeliveryInfo.class))
                .build();
        orders.put(order.getOrderId(), order);
        // When
        new PreparationTask(deliveryQueue, orders, order.getOrderId(), orderStatus).run();
        // Then
        assertSame(OrderStatus.READY_FOR_DELIVERY, orderStatus.get(order.getOrderId()));
        assertTrue(deliveryQueue.contains(order.getOrderId()));
    }
}