package org.pancakelab.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderStatus;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
        orders = new ConcurrentHashMap<>();
        orderStatus = new ConcurrentHashMap<>();
        kitchenService = new KitchenServiceImpl(orders, orderStatus);
    }

    @Test
    public void givenValidOrder_whenAcceptOrder_thenOrderStatusShouldBeInProgress() {
        // Given
        UUID orderId = UUID.randomUUID();
        OrderDetails orderDetails = mock(OrderDetails.class);
        when(orderDetails.getOrderId()).thenReturn(orderId);
        orders.put(orderId, orderDetails);
        orderStatus.put(orderId, OrderStatus.READY_FOR_DELIVERY);
        // When
        kitchenService.acceptOrder(orderId);
        // Then
        Awaitility.await().until(()->OrderStatus.IN_PROGRESS.equals(orderStatus.get(orderId)));
    }

    @Test
    public void givenValidOrder_whenNotifyOrderCompletion_thenOrderStatusShouldBeReadyForDeliveryAndAddedToQueue() {
        // Given
        UUID orderId = UUID.randomUUID();
        OrderDetails orderDetails = mock(OrderDetails.class);
        when(orderDetails.getOrderId()).thenReturn(orderId);
        orders.put(orderId, orderDetails);
        orderStatus.put(orderId, OrderStatus.IN_PROGRESS);
        // When
        kitchenService.notifyOrderCompletion(orderId);
        // Then
        Awaitility.await().until(()->OrderStatus.READY_FOR_DELIVERY.equals(orderStatus.get(orderId)));
    }

    @Test
    public void givenOrders_whenViewOrders_thenShouldReturnAllOrders() {
        // Given
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        OrderDetails orderDetails1 = mock(OrderDetails.class);
        OrderDetails orderDetails2 = mock(OrderDetails.class);
        when(orderDetails1.getOrderId()).thenReturn(orderId1);
        when(orderDetails2.getOrderId()).thenReturn(orderId2);
        orders.put(orderId1, orderDetails1);
        orders.put(orderId2, orderDetails2);
        // When
        List<OrderDetails> allOrders = kitchenService.viewOrders();
        // Then
        assertEquals(2, allOrders.size());
        assertTrue(allOrders.contains(orderDetails1));
        assertTrue(allOrders.contains(orderDetails2));
    }
}