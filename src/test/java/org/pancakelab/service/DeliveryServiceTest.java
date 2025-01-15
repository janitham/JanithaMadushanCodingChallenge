package org.pancakelab.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeliveryServiceTest {

    private ConcurrentHashMap<UUID, OrderDetails> orders;
    private ConcurrentHashMap<UUID, OrderStatus> orderStatus;
    private DeliveryService deliveryService;
    private BlockingDeque<UUID> deliveriesQueue;
    private User user;

    @BeforeEach
    public void setUp() {
        orders = new ConcurrentHashMap<>();
        orderStatus = new ConcurrentHashMap<>();
        deliveriesQueue = new LinkedBlockingDeque<>();
        deliveryService = new DeliveryServiceImpl(orders, orderStatus, deliveriesQueue, 2);
        user = new User("user", "password".toCharArray(), new HashMap<>());
    }

    @Test
    void givenValidOrder_whenAcceptOrder_thenOrderStatusShouldBeOutForDelivery() throws PancakeServiceException {
        // Given
        final UUID orderId = UUID.randomUUID();
        final OrderDetails orderDetails = mock(OrderDetails.class);
        when(orderDetails.getOrderId()).thenReturn(orderId);
        orders.put(orderId, orderDetails);
        orderStatus.put(orderId, OrderStatus.READY_FOR_DELIVERY);
        // When
        deliveryService.acceptOrder(user, orderId);
        // Then
        Awaitility.await().until(() -> OrderStatus.OUT_FOR_DELIVERY.equals(orderStatus.get(orderId)));
    }

    @Test
    void givenValidOrder_whenSendForTheDelivery_thenOrderStatusShouldBeDelivered() throws PancakeServiceException {
        // Given
        final UUID orderId = UUID.randomUUID();
        final OrderDetails orderDetails = mock(OrderDetails.class);
        when(orderDetails.getOrderId()).thenReturn(orderId);
        orders.put(orderId, orderDetails);
        orderStatus.put(orderId, OrderStatus.OUT_FOR_DELIVERY);
        // When
        deliveryService.sendForTheDelivery(user, orderId);
        // Then
        Awaitility.await().until(() -> OrderStatus.DELIVERED.equals(orderStatus.get(orderId)));
        assertFalse(orders.containsKey(orderId));
    }

    @Test
    void givenValidOrder_whenViewCompletedOrders_thenShouldReturnCompletedOrders() throws InterruptedException {
        // Given
        final UUID orderId1 = UUID.randomUUID();
        final OrderDetails orderDetails1 = mock(OrderDetails.class);
        final DeliveryInfo deliveryInfo1 = mock(DeliveryInfo.class);
        when(orderDetails1.getOrderId()).thenReturn(orderId1);
        // When
        when(orderDetails1.getDeliveryInfo()).thenReturn(deliveryInfo1);
        orders.put(orderId1, orderDetails1);
        orderStatus.put(orderId1, OrderStatus.READY_FOR_DELIVERY);
        deliveriesQueue.put(orderId1);
        // Then
        Awaitility.await().until(() ->
                {
                    var completedOrders = deliveryService.viewCompletedOrders(user);
                    return completedOrders.size() == 1;
                }
        );
    }
}
