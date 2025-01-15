package org.pancakelab.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KitchenServiceTest {

    private ConcurrentHashMap<UUID, OrderDetails> orders;
    private ConcurrentHashMap<UUID, OrderStatus> orderStatus;
    private KitchenService kitchenService;

    @BeforeEach
    public void setUp() {
        BlockingDeque<UUID> ordersQueue = new LinkedBlockingDeque<>();
        orders = new ConcurrentHashMap<>();
        orderStatus = new ConcurrentHashMap<>();
        kitchenService = new KitchenServiceImpl(orders, orderStatus, ordersQueue);
    }

    @Test
    public void givenValidOrder_whenAcceptOrder_thenOrderStatusShouldBeInProgress() throws PancakeServiceException {
        // Given
        UUID orderId = UUID.randomUUID();
        OrderDetails orderDetails = mock(OrderDetails.class);
        when(orderDetails.getOrderId()).thenReturn(orderId);
        orders.put(orderId, orderDetails);
        orderStatus.put(orderId, OrderStatus.READY_FOR_DELIVERY);
        // When
        kitchenService.acceptOrder(null, orderId);
        // Then
        Awaitility.await().until(() -> OrderStatus.IN_PROGRESS.equals(orderStatus.get(orderId)));
    }

    @Test
    public void givenValidOrder_whenNotifyOrderCompletion_thenOrderStatusShouldBeReadyForDeliveryAndAddedToQueue() throws PancakeServiceException {
        // Given
        UUID orderId = UUID.randomUUID();
        OrderDetails orderDetails = mock(OrderDetails.class);
        when(orderDetails.getOrderId()).thenReturn(orderId);
        orders.put(orderId, orderDetails);
        orderStatus.put(orderId, OrderStatus.IN_PROGRESS);
        // When
        kitchenService.notifyOrderCompletion(null, orderId);
        // Then
        Awaitility.await().until(() -> OrderStatus.READY_FOR_DELIVERY.equals(orderStatus.get(orderId)));
    }

    @Test
    public void givenOrders_whenViewOrders_thenShouldReturnAllOrders() throws PancakeServiceException {
        // Given
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        OrderDetails orderDetails1 = mock(OrderDetails.class);
        OrderDetails orderDetails2 = mock(OrderDetails.class);
        when(orderDetails1.getOrderId()).thenReturn(orderId1);
        when(orderDetails2.getOrderId()).thenReturn(orderId2);
        orders.put(orderId1, orderDetails1);
        orders.put(orderId2, orderDetails2);

        Map<Pancakes, Integer> pancakeItems1 = new ConcurrentHashMap<>();
        Map<Pancakes, Integer> pancakeItems2 = new ConcurrentHashMap<>();
        when(orderDetails1.getPancakes()).thenReturn(pancakeItems1);
        when(orderDetails2.getPancakes()).thenReturn(pancakeItems2);

        // Simulate order processing
        kitchenService.notifyOrderCompletion(null, orderId1);
        kitchenService.notifyOrderCompletion(null, orderId2);

        // When
        Awaitility.await().until(() -> !kitchenService.viewOrders(null).isEmpty());
        Map<UUID, Map<PancakeRecipe, Integer>> allOrders = kitchenService.viewOrders(null);

        // Then
        assertEquals(2, allOrders.size());
        assertTrue(allOrders.containsKey(orderId1));
        assertTrue(allOrders.containsKey(orderId2));
        assertEquals(pancakeItems1, allOrders.get(orderId1));
        assertEquals(pancakeItems2, allOrders.get(orderId2));
    }
}