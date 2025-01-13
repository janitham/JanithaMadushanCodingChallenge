package org.pancakelab.itest;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;
import org.pancakelab.service.*;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeOrderSuccessfulProcessingTest {

    private static final ConcurrentMap<UUID, OrderDetails> orders = new ConcurrentHashMap<>();
    private static final BlockingDeque<UUID> deliveryQueue = new LinkedBlockingDeque<>();
    private static Thread deliveryService;
    private static KitchenService kitchenService;
    private static OrderService orderService;
    private static UUID orderId;
    private static DeliveryInfo deliveryInfo;
    private static ConcurrentHashMap<UUID, OrderStatus> orderStatus;
    private static final User authorizedUser = new User("testUser", "password".toCharArray());

    @BeforeAll
    public static void init() {
        initializeDeliveryInfo();
        initializeOrderStatus();
        initializeDeliveryService();
        initializeKitchenService();
        initializeOrderService();
    }

    @Test
    @Order(1)
    public void whenValidPancakeOrderIsPlaced_thenOrderShouldBePlaced() throws PancakeServiceException {
        orderId = orderService.createOrder(authorizedUser, deliveryInfo);
        assertNotNull(orderId);
    }

    @Test
    @Order(2)
    public void whenOrderIsUpdatedWithItemsInTheMenu_thenOrderShouldContainTheItems() throws PancakeServiceException {
        var pancakes = Map.of(
                PancakeMenu.DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE, 1,
                PancakeMenu.MILK_CHOCOLATE_PANCAKE, 2
        );
        orderService.addPancakes(authorizedUser, orderId, pancakes);
        assertEquals(pancakes, orderService.orderSummary(authorizedUser, orderId));
    }

    @Test
    @Order(3)
    public void whenOrderIsCompleted_thenOrderShouldBeProcessedByTheKitchenAndRemoved() throws PancakeServiceException {
        orderService.complete(authorizedUser, orderId);
        Awaitility.await().until(() -> deliveryQueue.size() == 1);
        assertTrue(deliveryQueue.contains(orderId));
        assertEquals(orderStatus.get(orderId), OrderStatus.READY_FOR_DELIVERY);
    }

    @Test
    @Order(4)
    public void whenOrderIsReceivedByTheDeliveryService_thenOrderShouldBeDelivered() {
        deliveryService.start();
        Awaitility.await().until(deliveryQueue::isEmpty);
    }

    @AfterAll
    public static void tearDown() {
        deliveryService.interrupt();
        try {
            deliveryService.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        kitchenService.shutdown();
    }

    private static void initializeDeliveryInfo() {
        deliveryInfo = new DeliveryInfo("1", "2");
    }

    private static void initializeOrderStatus() {
        orderStatus = new ConcurrentHashMap<>();
    }

    private static void initializeDeliveryService() {
        deliveryService = new Thread(new DeliveryServiceImpl(orders, deliveryQueue, orderStatus));
    }

    private static void initializeKitchenService() {
        kitchenService = KitchenServiceImpl.getInstance(2, deliveryQueue, orders, orderStatus);
    }

    private static void initializeOrderService() {
        AuthenticationService authenticationService = new AuthenticationServiceImpl(new HashSet<>() {
            {
                add(authorizedUser);
            }
        });
        orderService = new AuthenticatedOrderService(
                new OrderServiceImpl(kitchenService, orders, orderStatus), authenticationService);
    }
}