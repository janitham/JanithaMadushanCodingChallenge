package org.pancakelab.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;
import org.pancakelab.util.DeliveryInformationValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.pancakelab.service.AuthorizedOrderService.ORDER_NOT_FOUND;
import static org.pancakelab.service.OrderServiceImpl.ORDER_CANNOT_BE_PROCESSED_WITHOUT_ORDER_ID;

public class OrderServiceTest {

    private ConcurrentHashMap<UUID, OrderDetails> orders;
    private OrderService orderService;
    private ConcurrentHashMap<UUID, OrderStatus> orderStatus;
    private User user;
    private DeliveryInformationValidator deliveryInformationValidator;
    private BlockingDeque<UUID> ordersQueue;
    ;

    private final Map<String, List<Character>> privileges = new HashMap<>() {
        {
            put("order", List.of('C', 'R', 'U', 'D'));
            put("kitchen", List.of('C', 'R', 'U', 'D'));
            put("delivery", List.of('C', 'R', 'U', 'D'));
        }
    };

    @BeforeEach
    public void setUp() {
        orders = new ConcurrentHashMap<>();
        orderStatus = new ConcurrentHashMap<>();
        ordersQueue = new LinkedBlockingDeque<>();
        deliveryInformationValidator = mock(DeliveryInformationValidator.class);
        orderService = new OrderServiceImpl(orders, orderStatus, deliveryInformationValidator, ordersQueue);
        user = new User("user", "password".toCharArray(), privileges);
    }

    @Test
    public void givenValidDeliveryInformation_then_orderShouldBePlaced() throws PancakeServiceException {
        // Given
        var deliveryInformation = new DeliveryInfo("1", "2");
        // When
        final UUID orderId = orderService.createOrder(user, deliveryInformation);
        // Then
        assertNotNull(orderId);
        assertEquals(orderService.status(user, orderId), OrderStatus.CREATED);
        verify(deliveryInformationValidator).validate(any());
    }

    @Test
    public void givenAlreadyCreatedOrder_then_creatingAnotherOrderWithTheSameDeliveryInformationThrowException()
            throws PancakeServiceException {
        // Given
        var deliveryInformation = new DeliveryInfo("1", "2");
        // When
        orderService.createOrder(user, deliveryInformation);
        // Then
        Exception exception = assertThrows(
                PancakeServiceException.class,
                () -> orderService.createOrder(user, deliveryInformation)
        );
        assertEquals(OrderServiceImpl.DUPLICATE_ORDERS_CANNOT_BE_PLACED, exception.getMessage());
    }

    @Test
    public void givenValidOrder_then_pancakesCanBeIncludedFromTheMenu() throws PancakeServiceException {
        // Given
        var orderId = orderService.createOrder(user, new DeliveryInfo("1", "2"));
        var pancakes1 = Map.of(
                Pancakes.DARK_CHOCOLATE_PANCAKE, 1,
                Pancakes.MILK_CHOCOLATE_PANCAKE, 2
        );
        var pancakes2 = Map.of(
                Pancakes.MILK_CHOCOLATE_PANCAKE, 1,
                Pancakes.MILK_CHOCOLATE_HAZELNUTS_PANCAKE, 4
        );
        orderService.addPancakes(user, orderId, pancakes1);
        orderService.addPancakes(user, orderId, pancakes2);
        // When
        final Map<Pancakes, Integer> summary = orderService.orderSummary(user, orderId);
        // Then
        assertEquals(1, summary.get(Pancakes.DARK_CHOCOLATE_PANCAKE));
        assertEquals(3, summary.get(Pancakes.MILK_CHOCOLATE_PANCAKE));
        assertEquals(4, summary.get(Pancakes.MILK_CHOCOLATE_HAZELNUTS_PANCAKE));
    }

    @Test
    public void givenInvalidOrderId_then_addingItemsShouldThrowException() {
        // Given
        // When
        // Then
        Exception exception = assertThrows(
                PancakeServiceException.class,
                () -> orderService.addPancakes(user, UUID.randomUUID(), new HashMap<>())
        );
        assertEquals(ORDER_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void givenNullOrderId_then_addingItemsShouldThrowException() {
        // Given
        // When
        // Then
        Exception exception = assertThrows(
                PancakeServiceException.class,
                () -> orderService.addPancakes(user, null, new HashMap<>())
        );
        assertEquals(ORDER_CANNOT_BE_PROCESSED_WITHOUT_ORDER_ID, exception.getMessage());
    }

    @Test
    public void givenValidOrderId_then_completingOrderShouldCompleteAsync() throws PancakeServiceException {
        // Given
        var orderId = orderService.createOrder(user, new DeliveryInfo("1", "2"));
        var pancakes1 = Map.of(Pancakes.DARK_CHOCOLATE_PANCAKE, 1);
        orderService.addPancakes(user, orderId, pancakes1);
        // When
        orderService.complete(user, orderId);
        // Then
        Awaitility.await().until(() -> orders.get(orderId) != null);
        Awaitility.await().until(() -> ordersQueue.contains(orderId));
    }

    @Test
    public void givenValidOrderId_then_cancel_shouldRemoveOrder() throws PancakeServiceException {
        // Given
        var orderId = orderService.createOrder(user, new DeliveryInfo("1", "2"));
        var pancakes1 = Map.of(Pancakes.DARK_CHOCOLATE_PANCAKE, 1);
        orderService.addPancakes(user, orderId, pancakes1);
        // When
        orderService.cancel(user, orderId);
        // Then
        Awaitility.await().until(() -> !orders.containsKey(orderId));
        Awaitility.await().until(() -> !ordersQueue.contains(orderId));
        assertEquals(OrderStatus.CANCELLED, orderStatus.get(orderId));
    }

    @Test
    public void givenNullOrderId_whenComplete_thenThrowException() {
        // Given
        // When
        // Then
        Exception exception = assertThrows(
                PancakeServiceException.class,
                () -> orderService.complete(user, null)
        );
        assertEquals(ORDER_CANNOT_BE_PROCESSED_WITHOUT_ORDER_ID, exception.getMessage());
    }

    @Test
    public void givenNonExistingOrderId_whenComplete_thenThrowException() {
        // Given
        // When
        // Then
        Exception exception = assertThrows(
                PancakeServiceException.class,
                () -> orderService.complete(user, UUID.randomUUID())
        );
        assertEquals(ORDER_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void givenMoreThan10Pancakes_whenAddPancakes_thenThrowException() throws PancakeServiceException {
        // Given
        var orderId = orderService.createOrder(user, new DeliveryInfo("1", "2"));
        var pancakes = Map.of(
                Pancakes.DARK_CHOCOLATE_PANCAKE, 10
        );
        // When
        // Then
        Exception exception = assertThrows(
                PancakeServiceException.class,
                () -> {
                    orderService.addPancakes(user, orderId, pancakes);
                    orderService.addPancakes(user, orderId, pancakes);
                }
        );
        assertEquals(OrderServiceImpl.MAXIMUM_PANCAKES_EXCEEDED, exception.getMessage());
    }

    @Test
    public void givenValidOrder_then_creatingAnotherOrderShouldThrowAnException() {
        // Given
        var user = new User("user2", "password2".toCharArray(), privileges);
        var orderId = UUID.randomUUID();
        orderStatus.put(orderId, OrderStatus.CREATED);
        orders.put(
                orderId,
                new OrderDetails.Builder().withOrderId(orderId).withPanCakes(
                        Map.of(
                                Pancakes.MILK_CHOCOLATE_PANCAKE, 1
                        )
                ).withDeliveryInfo(new DeliveryInfo("1", "2")
                ).withUser(user).build());
        // When
        // Then
        Exception exception = assertThrows(PancakeServiceException.class,
                () -> orderService.createOrder(user, new DeliveryInfo("1", "7")));
        assertEquals(OrderServiceImpl.USER_HAS_AN_ONGOING_ORDER, exception.getMessage());
    }

    @Test
    public void givenUserHasAnOngoingOrder_then_creatingAnotherOrderShouldThrowAnException() throws PancakeServiceException {
        // Given
        var orderId = UUID.randomUUID();
        orderStatus.put(orderId, OrderStatus.CREATED);
        orders.put(orderId, new OrderDetails.Builder()
                .withOrderId(orderId)
                .withUser(user)
                .withPanCakes(Map.of(
                        Pancakes.DARK_CHOCOLATE_PANCAKE, 1
                ))
                .withDeliveryInfo(new DeliveryInfo("1", "2"))
                .build());
        // When
        // Then
        Exception exception = assertThrows(PancakeServiceException.class,
                () -> orderService.createOrder(user, new DeliveryInfo("1", "7")));
        assertEquals(OrderServiceImpl.USER_HAS_AN_ONGOING_ORDER, exception.getMessage());
    }
}