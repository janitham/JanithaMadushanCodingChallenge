package org.pancakelab.itest;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;
import org.pancakelab.service.*;
import org.pancakelab.tasks.DeliveryPartnerTask;
import org.pancakelab.util.DeliveryInformationValidator;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeRecipeOrderSuccessfulProcessingTest {

    private static final ConcurrentMap<UUID, OrderDetails> orders = new ConcurrentHashMap<>();
    private static final BlockingDeque<UUID> deliveryQueue = new LinkedBlockingDeque<>();
    private static DeliveryService deliveryService;
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
        initializeKitchenService();
        initializeOrderService();
    }

    @Test
    @Order(1)
    public void whenValidPancakeOrderIsPlaced_thenOrderShouldBePlaced() throws PancakeServiceException {
        // Given
        orderId = orderService.createOrder(authorizedUser, deliveryInfo);
        // When
        // Then
        assertNotNull(orderId);
    }

    @Test
    @Order(2)
    public void whenOrderIsUpdatedWithItemsInTheMenu_thenOrderShouldContainTheItems() throws PancakeServiceException {
        // Given
        var pancakes = Map.of(
                PancakeMenu.DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE, 1,
                PancakeMenu.MILK_CHOCOLATE_PANCAKE, 2
        );
        // When
        orderService.addPancakes(authorizedUser, orderId, pancakes);
        // Then
        assertEquals(pancakes, orderService.orderSummary(authorizedUser, orderId));
    }

    @Test
    @Order(3)
    public void whenOrderIsCompleted_thenOrderShouldBeProcessedByTheKitchenAndRemoved() throws PancakeServiceException {
        // Given
        orderService.complete(authorizedUser, orderId);
        // When
        // Then
        Awaitility.await().until(() -> deliveryQueue.size() == 1);
        assertTrue(deliveryQueue.contains(orderId));
        assertEquals(orderStatus.get(orderId), OrderStatus.READY_FOR_DELIVERY);
    }

    @Test
    @Order(4)
    public void whenOrderIsReceivedByTheDeliveryService_thenOrderShouldBeDelivered() {
        // Given
        // When
        // Then
        initializeDeliveryService();
        Awaitility.await().until(deliveryQueue::isEmpty);
    }

    @AfterAll
    public static void tearDown() {
        deliveryService.shutdown();
        kitchenService.shutdown();
    }

    private static void initializeDeliveryInfo() {
        deliveryInfo = new DeliveryInfo("1", "2");
    }

    private static void initializeOrderStatus() {
        orderStatus = new ConcurrentHashMap<>();
    }

    private static void initializeDeliveryService() {
        deliveryService = new DeliveryService(1);
        deliveryService.registerDeliveryPartner(new DeliveryPartnerTask(orders, deliveryQueue, orderStatus));
    }

    private static void initializeKitchenService() {
        kitchenService = new KitchenService(1);
    }

    private static void initializeOrderService() {
        AuthenticationService authenticationService = new AuthenticationServiceImpl(new HashSet<>() {
            {
                add(authorizedUser);
            }
        });
        orderService = new AuthenticatedOrderService(
                new OrderServiceImpl(kitchenService, orders, orderStatus, new DeliveryInformationValidator(), deliveryQueue), authenticationService);
    }
}