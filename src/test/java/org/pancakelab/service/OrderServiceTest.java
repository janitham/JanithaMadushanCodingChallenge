package org.pancakelab.service;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;
import org.pancakelab.util.DeliveryInformationValidator;
import org.pancakelab.util.PancakeFactory;
import org.pancakelab.util.Pancakes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.pancakelab.service.AuthorizedOrderService.ORDER_NOT_FOUND;
import static org.pancakelab.service.OrderServiceImpl.ORDER_CANNOT_BE_PROCESSED_WITHOUT_ORDER_ID;

class OrderServiceTest {

    private ConcurrentHashMap<UUID, OrderDetails> ordersRepository;
    private ConcurrentHashMap<UUID, OrderStatus> orderStatusRepository;
    private BlockingDeque<UUID> ordersQueue;
    private OrderService orderService;
    private User user;
    private DeliveryInformationValidator deliveryInformationValidator;

    private final Map<String, List<Character>> privileges = new HashMap<>() {
        {
            put("order", List.of('C', 'R', 'U', 'D'));
            put("kitchen", List.of('C', 'R', 'U', 'D'));
            put("delivery", List.of('C', 'R', 'U', 'D'));
        }
    };

    @BeforeEach
    public void setUp() {
        ordersRepository = new ConcurrentHashMap<>();
        orderStatusRepository = new ConcurrentHashMap<>();
        ordersQueue = new LinkedBlockingDeque<>();
        deliveryInformationValidator = mock(DeliveryInformationValidator.class);
        final var recipeService = mock(RecipeService.class);
        orderService = new OrderServiceImpl(
                ordersRepository, orderStatusRepository, deliveryInformationValidator, ordersQueue, 10,
                recipeService);
        user = new User("user", "password".toCharArray(), privileges);
    }

    @Test
    void givenValidDeliveryInformation_then_orderShouldBePlaced() throws PancakeServiceException {
        // Given
        final var deliveryInformation = new DeliveryInfo("1", "2");
        // When
        final UUID orderId = orderService.createOrder(user, deliveryInformation);
        // Then
        assertNotNull(orderId);
        assertEquals(OrderStatus.CREATED, orderService.status(user, orderId));
        verify(deliveryInformationValidator).validate(any());
    }

    @Test
    void givenAlreadyCreatedOrder_then_creatingAnotherOrderWithTheSameDeliveryInformationThrowException()
            throws PancakeServiceException {
        // Given
        final var deliveryInformation = new DeliveryInfo("1", "2");
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
    void givenValidOrder_then_pancakesCanBeIncludedFromTheMenu() throws PancakeServiceException {
        // Given
        final var orderId = orderService.createOrder(user, new DeliveryInfo("1", "2"));
        final var pancakes1 = Map.of(
                PancakeFactory.get(Pancakes.DARK_CHOCOLATE_PANCAKE), 1,
                PancakeFactory.get(Pancakes.MILK_CHOCOLATE_PANCAKE), 2
        );
        final var pancakes2 = Map.of(
                PancakeFactory.get(Pancakes.MILK_CHOCOLATE_PANCAKE), 1,
                PancakeFactory.get(Pancakes.MILK_CHOCOLATE_HAZELNUTS_PANCAKE), 4
        );
        orderService.addPancakes(user, orderId, pancakes1);
        orderService.addPancakes(user, orderId, pancakes2);
        // When
        final Map<PancakeRecipe, Integer> summary = orderService.orderSummary(user, orderId);
        // Then
        assertEquals(1, summary.get(PancakeFactory.get(Pancakes.DARK_CHOCOLATE_PANCAKE)));
        assertEquals(3, summary.get(PancakeFactory.get(Pancakes.MILK_CHOCOLATE_PANCAKE)));
        assertEquals(4, summary.get(PancakeFactory.get(Pancakes.MILK_CHOCOLATE_HAZELNUTS_PANCAKE)));
    }

    @Test
    void givenInvalidOrderId_then_addingItemsShouldThrowException() {
        // Given
        // When
        // Then
        Exception exception = assertThrows(
                PancakeServiceException.class,
                () -> orderService.addPancakes(user, UUID.randomUUID(), Map.of())
        );
        assertEquals(ORDER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void givenNullOrderId_then_addingItemsShouldThrowException() {
        // Given
        // When
        // Then
        Exception exception = assertThrows(
                PancakeServiceException.class,
                () -> orderService.addPancakes(user, null, Map.of())
        );
        assertEquals(ORDER_CANNOT_BE_PROCESSED_WITHOUT_ORDER_ID, exception.getMessage());
    }

    @Test
    void givenValidOrderId_then_completingOrderShouldCompleteAsync() throws PancakeServiceException {
        // Given
        final var orderId = orderService.createOrder(user, new DeliveryInfo("1", "2"));
        final var pancakes1 = Map.of(
                PancakeFactory.get(Pancakes.DARK_CHOCOLATE_PANCAKE), 1
        );
        orderService.addPancakes(user, orderId, pancakes1);
        // When
        orderService.complete(user, orderId);
        // Then
        Awaitility.await().until(() -> ordersRepository.get(orderId) != null);
        Awaitility.await().until(() -> ordersQueue.contains(orderId));
    }

    @Test
    void givenValidOrderId_then_cancel_shouldRemoveOrder() throws PancakeServiceException {
        // Given
        final var orderId = orderService.createOrder(user, new DeliveryInfo("1", "2"));
        final var pancakes1 = Map.of(
                PancakeFactory.get(Pancakes.DARK_CHOCOLATE_PANCAKE), 1
        );
        orderService.addPancakes(user, orderId, pancakes1);
        // When
        orderService.cancel(user, orderId);
        // Then
        Awaitility.await().until(() -> !ordersRepository.containsKey(orderId));
        Awaitility.await().until(() -> !ordersQueue.contains(orderId));
        assertEquals(OrderStatus.CANCELLED, orderStatusRepository.get(orderId));
    }

    @Test
    void givenNullOrderId_whenComplete_thenThrowException() {
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
    void givenNonExistingOrderId_whenComplete_thenThrowException() {
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
    void givenMoreThan10Pancakes_whenAddPancakes_thenThrowException() throws PancakeServiceException {
        // Given
        final var orderId = orderService.createOrder(user, new DeliveryInfo("1", "2"));
        final var pancakes = Map.of(
                PancakeFactory.get(Pancakes.DARK_CHOCOLATE_PANCAKE), 10
        );
        // When
        // Then
        orderService.addPancakes(user, orderId, pancakes);
        Exception exception = assertThrows(
                PancakeServiceException.class,
                () -> orderService.addPancakes(user, orderId, pancakes)
        );
        assertEquals(OrderServiceImpl.MAXIMUM_PANCAKES_EXCEEDED, exception.getMessage());
    }

    @Test
    void givenValidOrder_then_creatingAnotherOrderShouldThrowAnException() {
        // Given
        final var user2 = new User("user2", "password2".toCharArray(), privileges);
        final var orderId = UUID.randomUUID();
        orderStatusRepository.put(orderId, OrderStatus.CREATED);
        ordersRepository.put(
                orderId,
                new OrderDetails.Builder().withOrderId(orderId).withPanCakes(
                        Map.of(
                                PancakeFactory.get(Pancakes.MILK_CHOCOLATE_PANCAKE), 1
                        )
                ).withDeliveryInfo(new DeliveryInfo("1", "2")
                ).withUser(user2).build());
        // When
        // Then
        Exception exception = assertThrows(PancakeServiceException.class,
                () -> orderService.createOrder(user2, new DeliveryInfo("1", "7")));
        assertEquals(OrderServiceImpl.USER_HAS_AN_ONGOING_ORDER, exception.getMessage());
    }

    @Test
    void givenUserHasAnOngoingOrder_then_creatingAnotherOrderShouldThrowAnException() {
        // Given
        final var orderId = UUID.randomUUID();
        orderStatusRepository.put(orderId, OrderStatus.CREATED);
        ordersRepository.put(orderId, new OrderDetails.Builder()
                .withOrderId(orderId)
                .withUser(user)
                .withPanCakes(
                        Map.of(
                                PancakeFactory.get(Pancakes.DARK_CHOCOLATE_PANCAKE), 1
                        )
                )
                .withDeliveryInfo(new DeliveryInfo("1", "2"))
                .build());
        // When
        // Then
        Exception exception = assertThrows(PancakeServiceException.class,
                () -> orderService.createOrder(user, new DeliveryInfo("1", "7")));
        assertEquals(OrderServiceImpl.USER_HAS_AN_ONGOING_ORDER, exception.getMessage());
    }
}