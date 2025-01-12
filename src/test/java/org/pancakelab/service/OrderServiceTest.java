package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;
import org.pancakelab.util.PancakeFactoryMenu;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.pancakelab.service.OrderServiceImpl.*;

public class OrderServiceTest {

    private ConcurrentMap<UUID, OrderInfo> orders;
    private KitchenService kitchenService;
    private OrderService orderService;

    @BeforeEach
    public void setUp() {
        orders = new ConcurrentHashMap<>();
        kitchenService = mock(KitchenService.class);
        orderService = new OrderServiceImpl(kitchenService, orders);
    }

    @Test
    public void givenDeliveryInformation_then_orderShouldBePlaced() throws PancakeServiceException {
        // Given
        var deliveryInformation = new DeliveryInfo("1", "2");
        // When
        final UUID orderId = orderService.createOrder(deliveryInformation);
        // Then
        assertNotNull(orderId);
    }

    @Test
    public void givenAlreadyGivenInformation_then_serviceShouldThrowException() throws PancakeServiceException {
        // Given
        var deliveryInformation = new DeliveryInfo("1", "2");
        orderService.createOrder(deliveryInformation);
        // When
        // Then
        Exception exception = assertThrows(
                PancakeServiceException.class,
                () -> orderService.createOrder(deliveryInformation)
        );
    }

    @Test
    public void givenValidOrder_then_pancakesCanBeIncludedFromTheMenu() throws PancakeServiceException {
        // Given
        var orderId = orderService.createOrder(new DeliveryInfo("1", "2"));
        var pancakes1 = new HashMap<PancakeFactoryMenu.PANCAKE_TYPE, Integer>() {
            {
                put(PancakeFactoryMenu.PANCAKE_TYPE.DARK_CHOCOLATE_PANCAKE, 1);
                put(PancakeFactoryMenu.PANCAKE_TYPE.MILK_CHOCOLATE_PANCAKE, 2);
            }
        };
        var pancakes2 = new HashMap<PancakeFactoryMenu.PANCAKE_TYPE, Integer>() {
            {
                put(PancakeFactoryMenu.PANCAKE_TYPE.MILK_CHOCOLATE_PANCAKE, 1);
                put(PancakeFactoryMenu.PANCAKE_TYPE.MILK_CHOCOLATE_HAZELNUTS_PANCAKE, 4);
            }
        };
        // When
        orderService.addPancakes(orderId, pancakes1);
        orderService.addPancakes(orderId, pancakes2);
        Map<PancakeFactoryMenu.PANCAKE_TYPE, Integer> summary = orderService.orderSummary(orderId);
        // Then
        assertEquals(summary.get(PancakeFactoryMenu.PANCAKE_TYPE.DARK_CHOCOLATE_PANCAKE), 1);
        assertEquals(summary.get(PancakeFactoryMenu.PANCAKE_TYPE.MILK_CHOCOLATE_PANCAKE), 3);
        assertEquals(summary.get(PancakeFactoryMenu.PANCAKE_TYPE.MILK_CHOCOLATE_HAZELNUTS_PANCAKE), 4);
    }

    @Test
    public void givenInvalidOrderId_then_addingItemsShouldThrowException() {
        // Given
        // When
        // Then
        Exception exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.addPancakes(UUID.randomUUID(), new HashMap<>())
        );
        assertEquals(ORDER_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void givenNullOrderId_then_addingItemsShouldThrowException() {
        // Given
        // When
        // Then
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.addPancakes(null, new HashMap<>())
        );
        assertEquals(ORDER_CANNNOT_BE_PROCESSED_WITHOUT_ORDER_ID, exception.getMessage());
    }

    @Test
    public void givenValidOrderDetails_whenOpenOrder_thenSuccessful() {
        // Given
        var orderDetails = new OrderDetails.Builder()
                .withDeliveryInfo(mock(DeliveryInfo.class))
                .addPancake(mock(Pancake.class))
                .build();
        // When
        var uuid = orderService.open(orderDetails);
        // Then
        assertNotNull(uuid);
    }

    @Test
    public void givenNoOrderDetails_whenOpen_thenThrowException() {
        // Given
        // When
        // Then
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.open(null)
        );
        assertEquals(ORDER_DETAILS_SHOULD_NOT_BE_NULL, exception.getMessage());
    }

    @Test
    public void givenExistingOrder_whenOpen_thenThrowException() {
        // Given
        var orderDetails = new OrderDetails.Builder()
                .withDeliveryInfo(mock(DeliveryInfo.class))
                .addPancake(mock(Pancake.class))
                .build();
        orders.put(orderDetails.getOrderId(), new OrderInfo(orderDetails, ORDER_STATUS.PENDING));
        // When
        // Then
        Exception exception = assertThrows(
                IllegalStateException.class, () -> orderService.open(orderDetails)
        );
        assertEquals(ORDER_CANNOT_BE_OPENED_WITH_THE_SAME_ORDER_ID, exception.getMessage());
    }

    @Test
    public void givenNullOrderId_whenComplete_thenThrowException() {
        // Given
        // When
        // Then
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.complete(null)
        );
        assertEquals(ORDER_CANNNOT_BE_PROCESSED_WITHOUT_ORDER_ID, exception.getMessage());
    }

    @Test
    public void givenNonExistingOrderId_whenComplete_thenThrowException() {
        // Given
        // When
        // Then
        Exception exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.complete(UUID.randomUUID())
        );
        assertEquals(ORDER_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void givenValidOrder_whenComplete_thenOrderShouldBeCompleted() {
        // Given
        var orderDetails = new OrderDetails.Builder()
                .withDeliveryInfo(mock(DeliveryInfo.class))
                .addPancake(mock(Pancake.class))
                .build();
        orders.put(orderDetails.getOrderId(), new OrderInfo(orderDetails, ORDER_STATUS.PENDING));
        // When
        orderService.complete(orderDetails.getOrderId());
        // Then
        verify(kitchenService).processOrder(orderDetails.getOrderId());
    }
}