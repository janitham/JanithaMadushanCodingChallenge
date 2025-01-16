package org.pancakelab.service;

import org.pancakelab.model.*;
import org.pancakelab.util.DeliveryInformationValidator;
import org.pancakelab.util.PancakeUtils;

import javax.sound.midi.Receiver;
import java.util.*;
import java.util.concurrent.*;

/**
 * Implementation of the OrderService interface.
 * This service handles the creation, modification, and completion of orders.
 * It uses a separate thread to process orders and manages order and delivery queues.
 * The service also validates delivery information and ensures that users do not have multiple ongoing orders.
 */
public class OrderServiceImpl implements OrderService {

    public static final String ORDER_NOT_FOUND = "Order not found";
    public static final String ORDER_CANNOT_BE_PROCESSED_WITHOUT_ORDER_ID = "Order id cannot be null";
    public static final String DUPLICATE_ORDERS_CANNOT_BE_PLACED = "Cannot create an order for the same delivery location";
    public static final Integer MAXIMUM_PANCAKES = 10;
    public static final String MAXIMUM_PANCAKES_EXCEEDED = "The maximum number of pancakes that can be ordered is %d".formatted(MAXIMUM_PANCAKES);
    public static final String USER_HAS_AN_ONGOING_ORDER = "The user has an ongoing order";

    private final ConcurrentMap<UUID, OrderDetails> ordersRepository;
    private final ConcurrentMap<UUID, OrderStatus> orderStatusRepository;
    private final DeliveryInformationValidator deliveryInformationValidator;
    private final ExecutorService executorService;
    private final BlockingDeque<UUID> ordersQueue;
    private final ConcurrentMap<DeliveryInfo, UUID> orderStorage = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Map<PancakeRecipe, Integer>> orderItemsLocalCache = new ConcurrentHashMap<>();
    private final RecipeService recipeService;

    /**
     * Constructs a new OrderServiceImpl.
     *
     * @param ordersRepository             the map of order details
     * @param orderStatusRepository        the map of order statuses
     * @param deliveryInformationValidator the validator for delivery information
     * @param ordersQueue                  the queue of orders to be processed
     * @param internalThreads              the number of internal threads to use
     */
    public OrderServiceImpl(
            final ConcurrentMap<UUID, OrderDetails> ordersRepository,
            final ConcurrentMap<UUID, OrderStatus> orderStatusRepository,
            final DeliveryInformationValidator deliveryInformationValidator,
            final BlockingDeque<UUID> ordersQueue,
            final Integer internalThreads,
            final RecipeService recipeService
    ) {
        this.ordersRepository = ordersRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.deliveryInformationValidator = deliveryInformationValidator;
        this.ordersQueue = ordersQueue;
        this.executorService = Executors.newFixedThreadPool(internalThreads);
        this.recipeService = recipeService;
    }

    /**
     * Creates a new order.
     *
     * @param user                the user creating the order
     * @param deliveryInformation the delivery information for the order
     * @return the UUID of the created order
     * @throws PancakeServiceException if the order cannot be created
     */
    @Override
    public UUID createOrder(User user, final DeliveryInfo deliveryInformation) throws PancakeServiceException {
        deliveryInformationValidator.validate(deliveryInformation);
        synchronized (this) {
            if (ordersRepository.values().stream().anyMatch(orderDetails ->
                    orderDetails.getUser().equals(user) && orderStatusRepository.containsKey(orderDetails.getOrderId()) &&
                            List.of(OrderStatus.CREATED, OrderStatus.READY_FOR_DELIVERY, OrderStatus.COMPLETED, OrderStatus.IN_PROGRESS, OrderStatus.OUT_FOR_DELIVERY)
                                    .contains(orderStatusRepository.get(orderDetails.getOrderId())))) {
                throw new PancakeServiceException(USER_HAS_AN_ONGOING_ORDER);
            }
        }
        final var orderId = UUID.randomUUID();
        synchronized (orderStorage) {
            if (orderStorage.putIfAbsent(deliveryInformation, orderId) != null) {
                throw new PancakeServiceException(DUPLICATE_ORDERS_CANNOT_BE_PLACED);
            }
        }
        synchronized (orderStatusRepository) {
            orderStatusRepository.put(orderId, OrderStatus.CREATED);
        }
        PancakeUtils.notifyUser(user, OrderStatus.CREATED);
        return orderId;
    }

    /**
     * Adds pancakes to an existing order.
     *
     * @param user     the user adding pancakes
     * @param orderId  the ID of the order to add pancakes to
     * @param pancakes the pancakes to add
     * @throws PancakeServiceException if the pancakes cannot be added
     */
    @Override
    public void addPancakes(User user, final UUID orderId, final Map<PancakeRecipe, Integer> pancakes) throws PancakeServiceException {
        validateOrderId(orderId);
        if (!recipeService.getRecipes(user).containsAll(pancakes.keySet())) {
            throw new PancakeServiceException("Pancakes not found");
        }
        synchronized (orderItemsLocalCache) {
            var currentTotal = orderItemsLocalCache.values().stream().mapToInt(item -> item.values().stream().mapToInt(Integer::intValue).sum()).sum();
            var incomingTotal = pancakes.values().stream().mapToInt(Integer::intValue).sum();
            if (currentTotal + incomingTotal > MAXIMUM_PANCAKES) {
                throw new PancakeServiceException(MAXIMUM_PANCAKES_EXCEEDED);
            }
        }
        synchronized (orderStorage) {
            if (!orderStorage.containsValue(orderId)) {
                throw new PancakeServiceException(ORDER_NOT_FOUND);
            }
        }
        synchronized (orderItemsLocalCache) {
            if (!orderItemsLocalCache.containsKey(orderId)) {
                orderItemsLocalCache.put(orderId, new ConcurrentHashMap<>(pancakes));
            } else {
                orderItemsLocalCache.merge(orderId, pancakes, (existing, incoming) -> {
                    incoming.forEach((recipe, count) -> existing.merge(recipe, count, Integer::sum));
                    return existing;
                });
            }
        }
    }

    /**
     * Provides a summary of an order.
     *
     * @param user    the user requesting the summary
     * @param orderId the ID of the order to summarize
     * @return a map of pancakes and their quantities
     * @throws PancakeServiceException if the order cannot be summarized
     */
    @Override
    public synchronized Map<PancakeRecipe, Integer> orderSummary(User user, final UUID orderId) throws PancakeServiceException {
        validateOrderId(orderId);
        final Map<PancakeRecipe, Integer> items = orderItemsLocalCache.get(orderId);
        if (items == null) {
            throw new PancakeServiceException(ORDER_NOT_FOUND);
        }
        return items;
    }

    /**
     * Gets the status of an order.
     *
     * @param user    the user requesting the status
     * @param orderId the ID of the order to get the status of
     * @return the status of the order
     */
    @Override
    public synchronized OrderStatus status(User user, UUID orderId) {
        return orderStatusRepository.get(orderId);
    }

    /**
     * Completes an order.
     *
     * @param user    the user completing the order
     * @param orderId the ID of the order to complete
     * @throws PancakeServiceException if the order cannot be completed
     */
    @Override
    public void complete(User user, final UUID orderId) throws PancakeServiceException {
        validateOrderId(orderId);
        if (!orderStorage.containsValue(orderId)) {
            throw new PancakeServiceException(ORDER_NOT_FOUND);
        }
        CompletableFuture.runAsync(() -> {
            synchronized (this) {
                var deliveryInfo = getDeliveryInfoByOrderId(orderId);
                var orderDetails = new OrderDetails.Builder()
                        .withDeliveryInfo(deliveryInfo)
                        .withOrderId(orderId)
                        .withUser(user)
                        .withPanCakes(orderItemsLocalCache.get(orderId))
                        .build();
                ordersRepository.put(orderId, orderDetails);
                orderStatusRepository.put(orderId, OrderStatus.COMPLETED);
                ordersQueue.add(orderId);
                cleanUpOrder(orderId, deliveryInfo);
                PancakeUtils.notifyUser(user, OrderStatus.COMPLETED);
            }
        }, executorService);
    }

    /**
     * Cancels an order.
     *
     * @param user    the user canceling the order
     * @param orderId the ID of the order to cancel
     * @throws PancakeServiceException if the order cannot be canceled
     */
    @Override
    public void cancel(User user, final UUID orderId) throws PancakeServiceException {
        validateOrderId(orderId);
        if (!orderStorage.containsValue(orderId)) {
            throw new IllegalStateException(ORDER_NOT_FOUND);
        }
        CompletableFuture.runAsync(() -> {
            synchronized (this) {
                var deliveryInfo = getDeliveryInfoByOrderId(orderId);
                cleanUpOrder(orderId, deliveryInfo);
                orderStatusRepository.put(orderId, OrderStatus.CANCELLED);
            }
            PancakeUtils.notifyUser(user, OrderStatus.CANCELLED);
        }, executorService);
    }

    /**
     * Validates the order ID.
     *
     * @param orderId the ID of the order to validate
     * @throws PancakeServiceException if the order ID is null
     */
    private void validateOrderId(final UUID orderId) throws PancakeServiceException {
        if (orderId == null) {
            throw new PancakeServiceException(ORDER_CANNOT_BE_PROCESSED_WITHOUT_ORDER_ID);
        }
    }

    /**
     * Gets the delivery information by order ID.
     *
     * @param orderId the ID of the order
     * @return the delivery information
     */
    private DeliveryInfo getDeliveryInfoByOrderId(final UUID orderId) {
        return orderStorage.entrySet().stream()
                .filter(entry -> entry.getValue().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(ORDER_NOT_FOUND))
                .getKey();
    }

    /**
     * Cleans up the order.
     *
     * @param orderId      the ID of the order to clean up
     * @param deliveryInfo the delivery information of the order
     */
    private synchronized void cleanUpOrder(final UUID orderId, final DeliveryInfo deliveryInfo) {
        orderStorage.remove(deliveryInfo);
        orderItemsLocalCache.remove(orderId);
    }

    /**
     * Shuts down the executor service, waiting for tasks to complete or forcing shutdown if necessary.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}