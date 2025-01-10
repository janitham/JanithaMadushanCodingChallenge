package org.pancakelab.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;

import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

public class PreparationTaskTest {
    private static final ConcurrentMap<UUID, OrderInfo> orders = new ConcurrentHashMap<>();
    private static final BlockingDeque<UUID> deliveryQueue = new LinkedBlockingDeque<>();

    @Test
    public void When_invalid_order_is_processed_then_warning_should_be_logged() throws Exception {
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
    }

    @Test
    public void when_order_is_not_found_then_warning_should_be_logged() throws Exception {
        // Given
        var orderId = UUID.randomUUID();

        // When
        final ORDER_STATUS status = new PreparationTask(deliveryQueue, orders, orderId).call();

        // Then
        assertSame(status, ORDER_STATUS.NOT_FOUND);
    }
}