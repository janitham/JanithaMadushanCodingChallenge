package org.pancakelab.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.DeliveryInfo;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderStatus;
import org.pancakelab.model.PancakeServiceException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeliveryServiceTest {

    private ConcurrentHashMap<UUID, OrderDetails> orders;
    private ConcurrentHashMap<UUID, OrderStatus> orderStatus;
    private DeliveryService deliveryService;

    @BeforeEach
    public void setUp() {
        orders = new ConcurrentHashMap<>();
        orderStatus = new ConcurrentHashMap<>();
        deliveryService = new DeliveryServiceImpl(orders, orderStatus);
    }

    @Test
    public void givenValidOrder_whenAcceptOrder_thenOrderStatusShouldBeOutForDelivery() throws PancakeServiceException {
        // Given
        UUID orderId = UUID.randomUUID();
        OrderDetails orderDetails = mock(OrderDetails.class);
        when(orderDetails.getOrderId()).thenReturn(orderId);
        orders.put(orderId, orderDetails);
        orderStatus.put(orderId, OrderStatus.READY_FOR_DELIVERY);
        // When
        deliveryService.acceptOrder(null, orderId);
        // Then
        Awaitility.await().until(() -> OrderStatus.OUT_FOR_DELIVERY.equals(orderStatus.get(orderId)));
    }

    @Test
    public void givenValidOrder_whenSendForTheDelivery_thenOrderStatusShouldBeDelivered() throws PancakeServiceException {
        // Given
        UUID orderId = UUID.randomUUID();
        OrderDetails orderDetails = mock(OrderDetails.class);
        when(orderDetails.getOrderId()).thenReturn(orderId);
        orders.put(orderId, orderDetails);
        orderStatus.put(orderId, OrderStatus.OUT_FOR_DELIVERY);
        // When
        deliveryService.sendForTheDelivery(null, orderId);
        // Then
        Awaitility.await().until(() -> OrderStatus.DELIVERED.equals(orderStatus.get(orderId)));
        assertFalse(orders.containsKey(orderId));
    }

    @Test
    public void givenValidOrder_whenViewCompletedOrders_thenShouldReturnCompletedOrders() throws PancakeServiceException {
        // Given
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        OrderDetails orderDetails1 = mock(OrderDetails.class);
        OrderDetails orderDetails2 = mock(OrderDetails.class);
        when(orderDetails1.getOrderId()).thenReturn(orderId1);
        when(orderDetails2.getOrderId()).thenReturn(orderId2);
        DeliveryInfo deliveryInfo1 = mock(DeliveryInfo.class);
        DeliveryInfo deliveryInfo2 = mock(DeliveryInfo.class);
        when(orderDetails1.getDeliveryInfo()).thenReturn(deliveryInfo1);
        when(orderDetails2.getDeliveryInfo()).thenReturn(deliveryInfo2);
        orders.put(orderId1, orderDetails1);
        orders.put(orderId2, orderDetails2);
        orderStatus.put(orderId1, OrderStatus.READY_FOR_DELIVERY);
        orderStatus.put(orderId2, OrderStatus.DELIVERED);

        // When
        Map<UUID, DeliveryInfo> completedOrders = deliveryService.viewCompletedOrders(null);

        // Then
        assertEquals(1, completedOrders.size());
        assertTrue(completedOrders.containsKey(orderId1));
        assertEquals(deliveryInfo1, completedOrders.get(orderId1));
        assertFalse(completedOrders.containsKey(orderId2));
    }
}
