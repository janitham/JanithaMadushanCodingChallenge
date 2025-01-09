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
    public void When_order_is_dispatched_Then_it_should_be_removed_from_the_database() {
        // Given
        final UUID orderId = UUID.randomUUID();
        final Logger mockLogger = mock(Logger.class);
        final var deliveryService = new DeliveryServiceImpl(orders, deliveryQueue, mockLogger);
        orders.put(orderId, new OrderDetails());
        deliveryQueue.add(orderId);

        // When
        new Thread(deliveryService).start();

        // Then
        Awaitility.await().until(orders::isEmpty);
        verify(mockLogger).info("Delivering order: 1");
    }

    @Test
    public void When_trying_to_deliver_an_order_that_does_not_exist_then_a_warning_should_be_logged() {
        // Given
        final UUID orderId = UUID.randomUUID();
        final Logger mockLogger = mock(Logger.class);
        final var deliveryService = new DeliveryServiceImpl(orders, deliveryQueue, mockLogger);
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