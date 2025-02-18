package org.pancakelab.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;
import org.pancakelab.util.PancakeFactory;
import org.pancakelab.util.Pancakes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KitchenServiceTest {

    private ConcurrentHashMap<UUID, OrderDetails> ordersRepository;
    private ConcurrentHashMap<UUID, OrderStatus> orderStatusRepository;
    private ConcurrentSkipListSet<PancakeRecipe> recipeRepository;
    private ChefService chefService;
    private BlockingDeque<UUID> ordersQueue;
    private BlockingDeque<UUID> deliveriesQueue;
    private User user;

    @BeforeEach
    public void setUp() {
        ordersRepository = new ConcurrentHashMap<>();
        orderStatusRepository = new ConcurrentHashMap<>();
        recipeRepository = new ConcurrentSkipListSet<>() {{
            add(PancakeFactory.get(Pancakes.MILK_CHOCOLATE_PANCAKE));
        }};
        ordersQueue = new LinkedBlockingDeque<>();
        deliveriesQueue = new LinkedBlockingDeque<>();
        chefService = new KitchenServiceImpl(
                ordersRepository, orderStatusRepository, recipeRepository, ordersQueue, deliveriesQueue, 2);
        user = new User("user", "password".toCharArray(), new HashMap<>());
    }

    @Test
    void givenValidOrder_whenAcceptOrder_thenOrderStatusShouldBeInProgress() throws PancakeServiceException {
        // Given
        final UUID orderId = UUID.randomUUID();
        final OrderDetails orderDetails = mock(OrderDetails.class);
        when(orderDetails.getOrderId()).thenReturn(orderId);
        ordersRepository.put(orderId, orderDetails);
        orderStatusRepository.put(orderId, OrderStatus.READY_FOR_DELIVERY);
        // When
        chefService.acceptOrder(user, orderId);
        // Then
        Awaitility.await().until(() -> OrderStatus.IN_PROGRESS.equals(orderStatusRepository.get(orderId)));
    }

    @Test
    void givenValidOrder_whenNotifyOrderCompletion_thenOrderStatusShouldBeReadyForDeliveryAndAddedToQueue() throws PancakeServiceException {
        // Given
        final UUID orderId = UUID.randomUUID();
        final OrderDetails orderDetails = mock(OrderDetails.class);
        when(orderDetails.getOrderId()).thenReturn(orderId);
        ordersRepository.put(orderId, orderDetails);
        orderStatusRepository.put(orderId, OrderStatus.IN_PROGRESS);
        // When
        chefService.notifyOrderCompletion(user, orderId);
        // Then
        Awaitility.await().until(() -> OrderStatus.READY_FOR_DELIVERY.equals(orderStatusRepository.get(orderId)));
    }

    @Test
    void givenOrders_whenViewOrders_thenShouldReturnAllOrders() throws PancakeServiceException {
        // Given
        final UUID orderId1 = UUID.randomUUID();
        final OrderDetails orderDetails1 = mock(OrderDetails.class);
        when(orderDetails1.getOrderId()).thenReturn(orderId1);
        ordersRepository.put(orderId1, orderDetails1);

        final Map<PancakeRecipe, Integer> pancakeItems1 = new ConcurrentHashMap<>() {{
            put(PancakeFactory.get(Pancakes.DARK_CHOCOLATE_PANCAKE),1);
            put(PancakeFactory.get(Pancakes.MILK_CHOCOLATE_PANCAKE),2);
        }};
        when(orderDetails1.getPancakes()).thenReturn(pancakeItems1);

        // When
        ordersQueue.add(orderId1);

        // Then
        Awaitility.await().until(() -> chefService.viewOrders(user).size() == 1);
        var pancakesList = chefService.viewOrders(null).values().stream()
                .flatMap(map -> map.keySet().stream())
                .toList();
        assertEquals(2, pancakesList.size());
    }
}