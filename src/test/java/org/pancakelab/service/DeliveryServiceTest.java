package org.pancakelab.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;
import org.pancakelab.tasks.DeliveryPartnerTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.mockito.Mockito.mock;

public class DeliveryServiceTest {

    private static BlockingDeque<UUID> deliveryQueue;
    private static ConcurrentMap<UUID, OrderDetails> orders;
    private static ConcurrentMap<UUID, OrderStatus> orderStatus;
    private static DeliveryService deliveryService;

    @BeforeAll
    public static void init() {
        orders = new ConcurrentHashMap<>();
        deliveryQueue = new LinkedBlockingDeque<>();
        orderStatus = new ConcurrentHashMap<>();
        deliveryService = new DeliveryService(
                1
        );
        deliveryService.registerDeliveryPartner(new DeliveryPartnerTask(orders, deliveryQueue, orderStatus));
    }

    @Test
    public void testAddOrder() throws InterruptedException {
        // Given
        UUID orderId = UUID.randomUUID();
        OrderDetails orderDetails = new OrderDetails.Builder()
                .withDeliveryInfo(mock(DeliveryInfo.class))
                .withUser(mock(User.class))
                .withPanCakes(
                        Map.of(
                                PancakeMenu.DARK_CHOCOLATE_PANCAKE, 1
                        )
                )
                .build();
        // When
        orders.put(orderId, orderDetails);
        deliveryQueue.put(orderId);
        // Then
        Awaitility.await().until(() -> deliveryQueue.isEmpty());
    }

    @AfterAll
    public static void tearDown() {
        deliveryService.shutdown();
    }

}
