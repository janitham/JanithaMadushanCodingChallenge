package org.pancakelab.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.OrderDetails;

import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DeliveryServiceTest {

    private final ConcurrentMap<UUID, OrderDetails> orders = new ConcurrentHashMap<>();
    private final BlockingDeque<UUID> deliveryQueue = new LinkedBlockingDeque<>();

    @Test
    public void whenOrderIsDispatched_thenItShouldBeRemovedFromTheDatabase() {
        // Given
        var order = new OrderDetails();
        var mockLogger = mock(Logger.class);
        var deliveryService = new DeliveryServiceImpl(orders, deliveryQueue, mockLogger);
        orders.put(order.getOrderId(), order);
        deliveryQueue.add(order.getOrderId());

        // When
        new Thread(deliveryService).start();

        // Then
        Awaitility.await().until(orders::isEmpty);
        verify(mockLogger).info("Delivering order: " + order.getOrderId());
    }

    @Test
    public void whenTryingToDeliverAnOrderThatDoesNotExist_thenWarningShouldBeLogged() {
        // Given
        var orderId = UUID.randomUUID();
        var mockLogger = mock(Logger.class);
        var deliveryService = new DeliveryServiceImpl(orders, deliveryQueue, mockLogger);
        deliveryQueue.add(orderId);

        // When
        new Thread(deliveryService).start();

        // Then
        Awaitility.await().until(() -> {
            verify(mockLogger).warning("Order not found: " + orderId);
            return true;
        });
    }
}