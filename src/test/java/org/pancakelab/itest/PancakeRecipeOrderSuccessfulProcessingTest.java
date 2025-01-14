package org.pancakelab.itest;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;
import org.pancakelab.service.*;
import org.pancakelab.util.DeliveryInformationValidator;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PancakeRecipeOrderSuccessfulProcessingTest {

    private static ConcurrentHashMap<UUID, OrderDetails> orders;
    private static DeliveryServiceImpl deliveryService;
    private static KitchenServiceImpl kitchenService;
    private static OrderService orderService;
    private static UUID orderId;
    private static DeliveryInfo deliveryInfo;
    private static ConcurrentHashMap<UUID, OrderStatus> orderStatus;
    private static final User authorizedUser = new User("testUser", "password".toCharArray());

    @BeforeAll
    public static void init() {
        deliveryInfo = new DeliveryInfo("1", "2");
        orderStatus = new ConcurrentHashMap<>();
        orders = new ConcurrentHashMap<>();
        deliveryService = new DeliveryServiceImpl(orders, orderStatus);
        kitchenService = new KitchenServiceImpl(orders, orderStatus);
        AuthenticationService authenticationService = new AuthenticationServiceImpl(new HashSet<>() {
            {
                add(authorizedUser);
            }
        });
        orderService = new AuthenticatedOrderService(
                new OrderServiceImpl(orders, orderStatus, new DeliveryInformationValidator()), authenticationService);

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
                Pancakes.DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE, 1,
                Pancakes.MILK_CHOCOLATE_PANCAKE, 2
        );
        // When
        orderService.addPancakes(authorizedUser, orderId, pancakes);
        // Then
        assertEquals(pancakes, orderService.orderSummary(authorizedUser, orderId));
    }

    @Test
    @Order(3)
    public void whenOrderIsCompleted_thenOrderOrderStatusShouldBeChangedToCompleted() throws PancakeServiceException {
        // Given
        // When
        orderService.complete(authorizedUser, orderId);
        // Then
        Awaitility.await().until(() -> orderStatus.get(orderId).equals(OrderStatus.COMPLETED));
        // Given
        kitchenService.acceptOrder(orderId);
        // When
        // Then
        Awaitility.await().until(()->OrderStatus.IN_PROGRESS.equals(orderStatus.get(orderId)));
        // Given
        kitchenService.notifyOrderCompletion(orderId);
        // When
        // Then
        Awaitility.await().until(()->OrderStatus.READY_FOR_DELIVERY.equals(orderStatus.get(orderId)));
        // Given
        deliveryService.acceptOrder(orderId);
        // When
        // Then
        Awaitility.await().until(()->OrderStatus.OUT_FOR_DELIVERY.equals(orderStatus.get(orderId)));
        // Given
        deliveryService.sendForTheDelivery(orderId);
        // When
        // Then
        Awaitility.await().until(()->OrderStatus.DELIVERED.equals(orderStatus.get(orderId)));
    }

    @Test
    @Order(5)
    public void whenOrderIsReceivedByTheChef_thenOrderShouldBeInProgress() {

    }

    @AfterAll
    public static void tearDown() {
        deliveryService.shutdown();
        kitchenService.shutdown();
    }
}