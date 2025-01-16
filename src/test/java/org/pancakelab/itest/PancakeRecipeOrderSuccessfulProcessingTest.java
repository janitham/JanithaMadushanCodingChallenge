package org.pancakelab.itest;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.pancakelab.model.*;
import org.pancakelab.service.*;
import org.pancakelab.util.DeliveryInformationValidator;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PancakeRecipeOrderSuccessfulProcessingTest {

    private static DeliveryService deliveryService;
    private static KitchenService kitchenService;
    private static OrderService orderService;
    private static UUID orderId;
    private static DeliveryInfo deliveryInfo;
    public static final ConcurrentHashMap<UUID, OrderDetails> ordersRepository = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<UUID, OrderStatus> orderStatusRepository = new ConcurrentHashMap<>();;
    private static final Map<String, List<Character>> privileges = new HashMap<>() {
        {
            put("order", List.of('C', 'R', 'U', 'D'));
            put("kitchen", List.of('C', 'R', 'U', 'D'));
            put("delivery", List.of('C', 'R', 'U', 'D'));
        }
    };
    private static final User authorizedUser = new User("testUser", "password".toCharArray(), privileges);
    private static final BlockingDeque<UUID> ordersQueue = new LinkedBlockingDeque<>();
    private static final BlockingDeque<UUID> deliveriesQueue = new LinkedBlockingDeque<>();

    @BeforeAll
    public static void init() {
        deliveryInfo = new DeliveryInfo("1", "2");
        AuthenticationService authenticationService = new AuthenticationServiceImpl(new HashSet<>() {
            {
                add(authorizedUser);
            }
        });
        deliveryService = new AuthorizedDeliveryService(
                new DeliveryServiceImpl(ordersRepository, orderStatusRepository, deliveriesQueue, 2),
                authenticationService
        );
        kitchenService = new AuthorizedKitchenService(
                new KitchenServiceImpl(ordersRepository, orderStatusRepository, ordersQueue, deliveriesQueue, 2),
                authenticationService
        );
        orderService = new AuthorizedOrderService(
                new OrderServiceImpl(
                        ordersRepository, orderStatusRepository,
                        new DeliveryInformationValidator(), ordersQueue, 2),
                authenticationService);
    }

    @Test
    @Order(1)
    void givenValidPancakeOrder_whenOrderIsPlaced_thenOrderShouldBePlaced() throws PancakeServiceException {
        // Given
        orderId = orderService.createOrder(authorizedUser, deliveryInfo);
        // When
        // Then
        assertNotNull(orderId);
    }

    @Test
    @Order(2)
    void givenOrderWithItemsInTheMenu_whenOrderIsUpdated_thenOrderShouldContainTheItems() throws PancakeServiceException {
        // Given
        var pancakes = Map.of(
                Pancakes.DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE, 1,
                Pancakes.MILK_CHOCOLATE_PANCAKE, 2
        );
        // When
        orderService.addPancakes(authorizedUser, orderId, pancakes);
        // Then
        assertEquals(pancakes, orderService.orderSummary(authorizedUser, orderId));
    }

    @Test
    @Order(3)
    void givenOrder_whenOrderIsCompleted_thenOrderStatusShouldBeChangedToCompleted() throws PancakeServiceException {
        // Given
        // When
        orderService.complete(authorizedUser, orderId);
        // Then
        Awaitility.await().until(() -> orderStatusRepository.get(orderId).equals(OrderStatus.COMPLETED));
    }

    @Test
    @Order(5)
    void givenOrder_whenOrderIsReceivedByTheChef_thenOrderStatusShouldBeInProgress() throws PancakeServiceException {
        // Given
        kitchenService.acceptOrder(authorizedUser, orderId);
        // When
        // Then
        Awaitility.await().until(() -> OrderStatus.IN_PROGRESS.equals(orderStatusRepository.get(orderId)));
    }

    @Test
    @Order(6)
    void givenOrder_whenOrderIsCompletedByTheChef_thenOrderStatusShouldBeReadyForDelivery() throws PancakeServiceException {
        // Given
        kitchenService.notifyOrderCompletion(authorizedUser, orderId);
        // When
        // Then
        Awaitility.await().until(() -> OrderStatus.READY_FOR_DELIVERY.equals(orderStatusRepository.get(orderId)));
    }

    @Test
    @Order(7)
    void givenOrder_whenOrderIsAcceptedByDeliveryService_thenOrderStatusShouldBeOutForDelivery() throws PancakeServiceException {
        // Given
        deliveryService.acceptOrder(authorizedUser, orderId);
        // When
        // Then
        Awaitility.await().until(() -> OrderStatus.OUT_FOR_DELIVERY.equals(orderStatusRepository.get(orderId)));
    }

    @Test
    @Order(8)
    void givenOrder_whenOrderIsSentForDelivery_thenOrderStatusShouldBeDelivered() throws PancakeServiceException {
        // Given
        deliveryService.sendForTheDelivery(authorizedUser, orderId);
        // When
        // Then
        Awaitility.await().until(() -> OrderStatus.DELIVERED.equals(orderStatusRepository.get(orderId)));
    }

    /*@AfterAll
    public static void tearDown() {
        ((DeliveryServiceImpl) deliveryService).shutdown();
        ((KitchenServiceImpl) kitchenService).shutdown();
    }*/
}