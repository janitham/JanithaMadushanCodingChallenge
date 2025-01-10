package org.pancakelab.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;

import java.util.UUID;
import java.util.concurrent.*;

import static org.mockito.Mockito.mock;

public class KitchenServiceTest {

    private static KitchenService kitchenService;
    private static final ConcurrentMap<UUID, OrderInfo> orders = new ConcurrentHashMap<>();
    private static final BlockingDeque<UUID> deliveryQueue = new LinkedBlockingDeque<>();

    @BeforeAll
    public static void init() {
        kitchenService = KitchenServiceImpl.getInstance(5, deliveryQueue, orders);
    }

    @Test
    public void whenOrderIsProcessed_thenItShouldBeReadyForDelivery() {
        // Given
        var order = new OrderDetails.Builder()
                .addPancake(mock(Pancake.class))
                .withDeliveryInfo(mock(DeliveryInfo.class))
                .build();
        orders.put(order.getOrderId(), new OrderInfo(order, ORDER_STATUS.PENDING));

        // When
        kitchenService.processOrder(order.getOrderId());

        // Then
        Awaitility.await().until(() -> orders.get(order.getOrderId()).getStatus() == ORDER_STATUS.READY_FOR_DELIVERY);
        Awaitility.await().until(() -> deliveryQueue.contains(order.getOrderId()));
    }



    @AfterAll
    public static void tearDown() {
        kitchenService.shutdown();
    }
}