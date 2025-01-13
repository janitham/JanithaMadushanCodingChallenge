package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.pancakelab.service.OrderServiceImpl.ORDER_CANNOT_BE_PROCESSED_WITHOUT_ORDER_ID;
import static org.pancakelab.service.OrderServiceImpl.ORDER_NOT_FOUND;

public class OrderServiceTest {

    private ConcurrentMap<UUID, OrderDetails> orders;
    private KitchenService kitchenService;
    private OrderService orderService;
    private ConcurrentHashMap<UUID, OrderStatus> orderStatus;

    @BeforeEach
    public void setUp() {
        orders = new ConcurrentHashMap<>();
        kitchenService = mock(KitchenService.class);
        orderStatus = new ConcurrentHashMap<>();
        orderService = new OrderServiceImpl(kitchenService, orders, orderStatus);
    }

    @Test
    public void givenValidDeliveryInformation_then_orderShouldBePlaced() throws PancakeServiceException {
        // Given
        var deliveryInformation = new DeliveryInfo("1", "2");
        // When
        final UUID orderId = orderService.createOrder(deliveryInformation);
        // Then
        assertNotNull(orderId);
        assertEquals(orderService.status(orderId), ORDER_STATUS.CREATED);
    }

    @Test
    public void givenAlreadyCreatedOrder_then_creatingAnotherOrderWithTheSameDeliveryInformationThrowException()
            throws PancakeServiceException {
        // Given
        var deliveryInformation = new DeliveryInfo("1", "2");
        // When
        orderService.createOrder(deliveryInformation);
        // Then
        Exception exception = assertThrows(
                PancakeServiceException.class,
                () -> orderService.createOrder(deliveryInformation)
        );
        assertEquals(OrderServiceImpl.DUPLICATE_ORDERS_CANNOT_BE_PLACED, exception.getMessage());
    }

    @Test
    public void givenValidOrder_then_pancakesCanBeIncludedFromTheMenu() throws PancakeServiceException {
        // Given
        var orderId = orderService.createOrder(new DeliveryInfo("1", "2"));
        var pancakes1 = Map.of(
                PancakeMenu.DARK_CHOCOLATE_PANCAKE, 1,
                PancakeMenu.MILK_CHOCOLATE_PANCAKE, 2
        );
        var pancakes2 = Map.of(
                PancakeMenu.MILK_CHOCOLATE_PANCAKE, 1,
                PancakeMenu.MILK_CHOCOLATE_HAZELNUTS_PANCAKE, 4
        );
        orderService.addPancakes(orderId, pancakes1);
        orderService.addPancakes(orderId, pancakes2);
        // When
        final Map<PancakeMenu, Integer> summary = orderService.orderSummary(orderId);
        // Then
        assertEquals(1, summary.get(PancakeMenu.DARK_CHOCOLATE_PANCAKE));
        assertEquals(3, summary.get(PancakeMenu.MILK_CHOCOLATE_PANCAKE));
        assertEquals(4, summary.get(PancakeMenu.MILK_CHOCOLATE_HAZELNUTS_PANCAKE));
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
        assertEquals(ORDER_CANNOT_BE_PROCESSED_WITHOUT_ORDER_ID, exception.getMessage());
    }

    @Test
    public void givenValidOrderId_then_completingOrderShouldReturnFutureObject() throws PancakeServiceException {
        // Given
        var orderId = orderService.createOrder(new DeliveryInfo("1", "2"));
        var pancakes1 = Map.of(PancakeMenu.DARK_CHOCOLATE_PANCAKE, 1);
        orderService.addPancakes(orderId, pancakes1);
        // When
        orderService.complete(orderId);
        // Then
        assertNotNull(orders.get(orderId));
        assertThrows(IllegalStateException.class, () -> orderService.orderSummary(orderId));
        verify(kitchenService).processOrder(orderId);
    }

    @Test
    public void givenValidOrderId_then_cancel_shouldRemoveOrder() throws PancakeServiceException {
        // Given
        var orderId = orderService.createOrder(new DeliveryInfo("1", "2"));
        var pancakes1 = Map.of(PancakeMenu.DARK_CHOCOLATE_PANCAKE, 1);
        orderService.addPancakes(orderId, pancakes1);
        // When
        orderService.cancel(orderId);
        // Then
        assertFalse(orders.containsKey(orderId));
        assertThrows(IllegalStateException.class, () -> orderService.orderSummary(orderId));
        assertEquals(ORDER_STATUS.CANCELLED, orderStatus.get(orderId));
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
        assertEquals(ORDER_CANNOT_BE_PROCESSED_WITHOUT_ORDER_ID, exception.getMessage());
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
}