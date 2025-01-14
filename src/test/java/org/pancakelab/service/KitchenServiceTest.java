package org.pancakelab.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;
import org.pancakelab.tasks.PreparationTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@Disabled
public class KitchenServiceTest {

    private static KitchenService kitchenService;
    private static final ConcurrentMap<UUID, OrderDetails> orders = new ConcurrentHashMap<>();
    private static final BlockingDeque<UUID> deliveryQueue = new LinkedBlockingDeque<>();
    private static final ConcurrentHashMap<UUID, OrderStatus> orderStatus = new ConcurrentHashMap<>();

    @BeforeAll
    public static void init() {
        kitchenService = new KitchenService(1);
    }

    @Test
    public void givenOrderIsPending_whenProcessed_thenItShouldBeReadyForDelivery() throws ExecutionException, InterruptedException {
        // Given
        var order = new OrderDetails.Builder()
                .withPanCakes(Map.of(PancakeMenu.DARK_CHOCOLATE_PANCAKE, 2))
                .withDeliveryInfo(mock(DeliveryInfo.class))
                .withUser(mock(User.class))
                .build();
        orders.put(order.getOrderId(), order);
        // When
        kitchenService.submitTask(new PreparationTask(deliveryQueue, orders, order.getOrderId(), orderStatus));
        // Then
        Awaitility.await().until(() -> orderStatus.get(order.getOrderId()) == OrderStatus.READY_FOR_DELIVERY);
        assertTrue(deliveryQueue.contains(order.getOrderId()));
    }

    @AfterAll
    public static void tearDown() {
        kitchenService.shutdown();
    }
}