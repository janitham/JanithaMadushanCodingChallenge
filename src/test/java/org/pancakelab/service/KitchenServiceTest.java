package org.pancakelab.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;

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
    private static final ConcurrentHashMap<UUID, ORDER_STATUS> orderStatus = new ConcurrentHashMap<>();

    @BeforeAll
    public static void init() {
        kitchenService = KitchenServiceImpl.getInstance(5, deliveryQueue, orders, orderStatus);
    }

    @Test
    public void givenOrderIsPending_whenProcessed_thenItShouldBeReadyForDelivery() throws ExecutionException, InterruptedException {
        // Given
        var order = new OrderDetails.Builder()
                .withPanCakes(Map.of(PancakeMenu.DARK_CHOCOLATE_PANCAKE, 2))
                .withDeliveryInfo(mock(DeliveryInfo.class))
                .build();
        orders.put(order.getOrderId(), order);
        // When
        kitchenService.processOrder(order.getOrderId());
        // Then
        Awaitility.await().until(() -> orderStatus.get(order.getOrderId()) == ORDER_STATUS.READY_FOR_DELIVERY);
        assertTrue(deliveryQueue.contains(order.getOrderId()));
    }

    @AfterAll
    public static void tearDown() {
        kitchenService.shutdown();
    }
}