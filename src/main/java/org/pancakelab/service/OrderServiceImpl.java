package org.pancakelab.service;

import org.pancakelab.model.*;
import org.pancakelab.util.DeliveryInformationValidator;
import org.pancakelab.util.PancakeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class OrderServiceImpl implements OrderService {

    public static final String ORDER_NOT_FOUND = "Order not found";
    public static final String ORDER_CANNOT_BE_PROCESSED_WITHOUT_ORDER_ID = "Order id cannot be null";
    public static final String DUPLICATE_ORDERS_CANNOT_BE_PLACED = "Cannot create an order for the same delivery location";
    public static final Integer MAXIMUM_PANCAKES = 10;
    public static final String MAXIMUM_PANCAKES_EXCEEDED = "The maximum number of pancakes that can be ordered is %d".formatted(MAXIMUM_PANCAKES);
    public static final String USER_HAS_AN_ONGOING_ORDER = "The user has an ongoing order";

    private final ConcurrentMap<UUID, OrderDetails> orders;
    private final ConcurrentHashMap<UUID, OrderStatus> orderStatus;
    private final DeliveryInformationValidator deliveryInformationValidator;
    private final ConcurrentHashMap<DeliveryInfo, UUID> orderStorage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Map<Pancakes, Integer>> orderItems = new ConcurrentHashMap<>();
    private final ExecutorService executorService;

    public OrderServiceImpl(
            final ConcurrentMap<UUID, OrderDetails> orders,
            final ConcurrentHashMap<UUID, OrderStatus> orderStatus,
            final DeliveryInformationValidator deliveryInformationValidator
    ) {
        this.orders = orders;
        this.orderStatus = orderStatus;
        this.deliveryInformationValidator = deliveryInformationValidator;
        this.executorService = Executors.newFixedThreadPool(10); // Example thread pool size
    }

    @Override
    public UUID createOrder(User user, final DeliveryInfo deliveryInformation) throws PancakeServiceException {
        deliveryInformationValidator.validate(deliveryInformation);
        var orderId = UUID.randomUUID();
        if (orderStorage.putIfAbsent(deliveryInformation, orderId) != null) {
            throw new PancakeServiceException(DUPLICATE_ORDERS_CANNOT_BE_PLACED);
        }
        if (orders.values().stream().anyMatch(orderDetails ->
                orderDetails.getUser().equals(user) && orderStatus.containsKey(orderDetails.getOrderId()) &&
                        List.of(OrderStatus.CREATED, OrderStatus.READY_FOR_DELIVERY)
                                .contains(orderStatus.get(orderDetails.getOrderId())))) {
            throw new PancakeServiceException(USER_HAS_AN_ONGOING_ORDER);
        }
        orderStatus.put(orderId, OrderStatus.CREATED);
        PancakeUtils.notifyUser(user, OrderStatus.CREATED);
        return orderId;
    }

    @Override
    public void addPancakes(User user, final UUID orderId, final Map<Pancakes, Integer> pancakes) throws PancakeServiceException {
        validateOrderId(orderId);
        var currentTotal = orderItems.values().stream().mapToInt(item -> item.values().stream().mapToInt(Integer::intValue).sum()).sum();
        var incomingTotal = pancakes.values().stream().mapToInt(Integer::intValue).sum();
        if (currentTotal + incomingTotal > MAXIMUM_PANCAKES) {
            throw new PancakeServiceException(MAXIMUM_PANCAKES_EXCEEDED);
        }
        if (!orderStorage.containsValue(orderId)) {
            throw new PancakeServiceException(ORDER_NOT_FOUND);
        }
        orderItems.merge(orderId, new HashMap<>(pancakes), (oldPancakes, newPancakes) -> {
            newPancakes.forEach((type, count) -> oldPancakes.merge(type, count, Integer::sum));
            return oldPancakes;
        });
    }

    @Override
    public Map<Pancakes, Integer> orderSummary(User user, final UUID orderId) throws PancakeServiceException {
        validateOrderId(orderId);
        final Map<Pancakes, Integer> items = orderItems.get(orderId);
        if (items == null) {
            throw new PancakeServiceException(ORDER_NOT_FOUND);
        }
        return new HashMap<>(items);
    }

    @Override
    public OrderStatus status(User user, UUID orderId) {
        return orderStatus.get(orderId);
    }

    @Override
    public void complete(User user, final UUID orderId) throws PancakeServiceException {
        validateOrderId(orderId);
        if (!orderStorage.containsValue(orderId)) {
            throw new PancakeServiceException(ORDER_NOT_FOUND);
        }
        CompletableFuture.runAsync(() -> {
            var deliveryInfo = getDeliveryInfoByOrderId(orderId);
            var orderDetails = new OrderDetails.Builder()
                    .withDeliveryInfo(deliveryInfo)
                    .withOrderId(orderId)
                    .withUser(user)
                    .withPanCakes(orderItems.get(orderId))
                    .build();
            orders.put(orderId, orderDetails);
            cleanUpOrder(orderId, deliveryInfo);
            orderStatus.put(orderId, OrderStatus.COMPLETED);
            PancakeUtils.notifyUser(user, OrderStatus.COMPLETED);
        }, executorService);
    }

    @Override
    public void cancel(User user, final UUID orderId) throws PancakeServiceException {
        validateOrderId(orderId);
        if (!orderStorage.containsValue(orderId)) {
            throw new IllegalStateException(ORDER_NOT_FOUND);
        }
        var deliveryInfo = getDeliveryInfoByOrderId(orderId);
        cleanUpOrder(orderId, deliveryInfo);
        orderStatus.put(orderId, OrderStatus.CANCELLED);
        CompletableFuture.runAsync(() -> {
            PancakeUtils.notifyUser(user, OrderStatus.CANCELLED);
        }, executorService);
    }

    private void validateOrderId(final UUID orderId) throws PancakeServiceException {
        if (orderId == null) {
            throw new PancakeServiceException(ORDER_CANNOT_BE_PROCESSED_WITHOUT_ORDER_ID);
        }
    }

    private DeliveryInfo getDeliveryInfoByOrderId(final UUID orderId) {
        return orderStorage.entrySet().stream()
                .filter(entry -> entry.getValue().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(ORDER_NOT_FOUND))
                .getKey();
    }

    private void cleanUpOrder(final UUID orderId, final DeliveryInfo deliveryInfo) {
        orderStorage.remove(deliveryInfo);
        orderItems.remove(orderId);
    }

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