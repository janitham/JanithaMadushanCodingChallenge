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

    @Test
    public void givenOrderNotFoundInTheDatabase_whenProcessed_thenWarningShouldBeLogged() {
        // Given
        UUID orderId = UUID.randomUUID();
        // When
        ORDER_STATUS status = new PreparationTask(deliveryQueue, orders, orderId).call();
        // Then
        assertSame(ORDER_STATUS.NOT_FOUND, status);
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
        ORDER_STATUS status = new PreparationTask(deliveryQueue, orders, order.getOrderId()).call();
        // Then
        assertSame(ORDER_STATUS.READY_FOR_DELIVERY, status);
        assertTrue(deliveryQueue.contains(order.getOrderId()));
    }
}