package org.pancakelab.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderStatus;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.Pancakes;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KitchenServiceTest {

    private ConcurrentHashMap<UUID, OrderDetails> orders;
    private ConcurrentHashMap<UUID, OrderStatus> orderStatus;
    private KitchenService kitchenService;
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Condition newOrderCondition = lock.newCondition();
    private static final BlockingDeque<UUID> ordersQueue = new LinkedBlockingDeque<>();

    @BeforeEach
    public void setUp() {
        orders = new ConcurrentHashMap<>();
        orderStatus = new ConcurrentHashMap<>();
        kitchenService = new KitchenServiceImpl(orders, orderStatus, ordersQueue, lock, newOrderCondition);
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
        Awaitility.await().until(() -> {
            lock.lock();
            try {
                return OrderStatus.IN_PROGRESS.equals(orderStatus.get(orderId));
            } finally {
                lock.unlock();
            }
        });
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
        Awaitility.await().until(() -> {
            lock.lock();
            try {
                return OrderStatus.READY_FOR_DELIVERY.equals(orderStatus.get(orderId));
            } finally {
                lock.unlock();
            }
        });
    }

    //@Test
    public void givenOrders_whenViewOrders_thenShouldReturnAllOrders() throws PancakeServiceException {
        // Given
        UUID orderId1 = UUID.randomUUID();
        OrderDetails orderDetails1 = mock(OrderDetails.class);
        when(orderDetails1.getOrderId()).thenReturn(orderId1);
        orders.put(orderId1, orderDetails1);

        Map<Pancakes, Integer> pancakeItems1 = new ConcurrentHashMap<>() {{
            put(Pancakes.DARK_CHOCOLATE_PANCAKE, 1);
            put(Pancakes.MILK_CHOCOLATE_PANCAKE, 2);
        }};
        when(orderDetails1.getPancakes()).thenReturn(pancakeItems1);

        // When
        ordersQueue.add(orderId1);

        // Then
        Awaitility.await().until(() -> kitchenService.viewOrders(null).size() == 1);
        var pancakesList = kitchenService.viewOrders(null).values().stream()
                .flatMap(map -> map.keySet().stream())
                .toList();
        assertTrue(pancakesList.size() == 2);
    }
}