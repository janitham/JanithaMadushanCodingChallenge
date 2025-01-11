package org.pancakelab.itest;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.DeliveryInfo;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderInfo;
import org.pancakelab.model.Pancake;
import org.pancakelab.service.*;

import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PancakeOrderSuccessfulProcessingTest {

    private static final ConcurrentMap<UUID, OrderInfo> orders = new ConcurrentHashMap<>();
    private static final BlockingDeque<UUID> deliveryQueue = new LinkedBlockingDeque<>();
    private static Thread deliveryService;
    private static KitchenService kitchenService;
    private static OrderService orderService;
    private static OrderDetails orderDetails;

    @BeforeAll
    public static void init() {
        orderDetails = new OrderDetails.Builder()
                .withDeliveryInfo(new DeliveryInfo("1", "2"))
                .addPancake(new Pancake.Builder().withChocolate(Pancake.CHOCOLATE.MILK).build())
                .build();
        deliveryService = new Thread(new DeliveryServiceImpl(orders, deliveryQueue));
        kitchenService = KitchenServiceImpl.getInstance(2, deliveryQueue, orders);
        orderService = new OrderServiceImpl(kitchenService, orders);
    }

    @Test
    @Order(1)
    public void whenValidPancakeOrderIsPlaced_thenOrderShouldBePlaced() {
        // Given
        // When
        UUID orderId = orderService.open(orderDetails);
        // Then
        assertEquals(1, orders.size());
        assertTrue(orders.containsKey(orderId));
    }

    @Test
    @Order(2)
    public void whenOrderIsCompleted_thenOrderShouldBeProcessedByTheKitchenAndRemoved() {
        // Given
        var orderId = orderDetails.getOrderId();
        // When
        orderService.complete(orderId);
        // Then
        Awaitility.await().until(() -> deliveryQueue.size() == 1);
        assertTrue(deliveryQueue.contains(orderId));
    }

    @Test
    @Order(3)
    public void whenOrderIsReceivedByTheDeliveryService_thenOrderShouldBeDelivered() {
        // Given
        // When
        deliveryService.start();
        // Then
        Awaitility.await().until(deliveryQueue::isEmpty);
    }

    @AfterAll
    public static void tearDown() {
        deliveryService.interrupt();
        kitchenService.shutdown();
    }
}
