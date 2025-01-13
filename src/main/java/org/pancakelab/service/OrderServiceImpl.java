package org.pancakelab.service;

import org.pancakelab.model.*;
import org.pancakelab.util.PancakeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class OrderServiceImpl implements OrderService {

    public static final String ORDER_DETAILS_SHOULD_NOT_BE_NULL = "Order details cannot be null";
    public static final String ORDER_NOT_FOUND = "Order not found";
    public static final String ORDER_CANNOT_BE_PROCESSED_WITHOUT_ORDER_ID = "Order id cannot be null";
    public static final String DUPLICATE_ORDERS_CANNOT_BE_PLACED = "Cannot create an order for the same delivery location";
    public static final Integer MAXIMUM_PANCAKES = 10;
    public static final String MAXIMUM_PANCAKES_EXCEEDED = "The maximum number of pancakes that can be ordered is %d".formatted(MAXIMUM_PANCAKES);

    private final KitchenService kitchenService;
    private final ConcurrentMap<UUID, OrderDetails> orders;
    private final ConcurrentHashMap<DeliveryInfo, UUID> orderStorage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Map<PancakeMenu, Integer>> orderItems = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, OrderStatus> orderStatus;

    public OrderServiceImpl(
            final KitchenService kitchenService,
            final ConcurrentMap<UUID, OrderDetails> orders,
            final ConcurrentHashMap<UUID, OrderStatus> orderStatus
    ) {
        this.kitchenService = kitchenService;
        this.orders = orders;
        this.orderStatus = orderStatus;
    }

    @Override
    public UUID createOrder(User user, final DeliveryInfo deliveryInformation) throws PancakeServiceException {
        validateDeliveryInfo(deliveryInformation);
        var orderId = UUID.randomUUID();
        if (orderStorage.putIfAbsent(deliveryInformation, orderId) != null) {
            throw new PancakeServiceException(DUPLICATE_ORDERS_CANNOT_BE_PLACED);
        }
        orderStatus.put(orderId, OrderStatus.CREATED);
        PancakeUtils.notifyUser(user, OrderStatus.CREATED);
        return orderId;
    }

    @Override
    public void addPancakes(User user, final UUID orderId, final Map<PancakeMenu, Integer> pancakes) throws PancakeServiceException {
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
    public Map<PancakeMenu, Integer> orderSummary(User user, final UUID orderId) throws PancakeServiceException {
        validateOrderId(orderId);
        final Map<PancakeMenu, Integer> items = orderItems.get(orderId);
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
        var deliveryInfo = getDeliveryInfoByOrderId(orderId);
        var orderDetails = new OrderDetails.Builder()
                .withDeliveryInfo(deliveryInfo)
                .withOrderId(orderId)
                .withUser(user)
                .withPanCakes(orderItems.get(orderId))
                .build();
        orders.put(orderId, orderDetails);
        cleanUpOrder(orderId, deliveryInfo);
        kitchenService.processOrder(orderId);
        PancakeUtils.notifyUser(user, OrderStatus.COMPLETED);
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
        PancakeUtils.notifyUser(user, OrderStatus.CANCELLED);
    }

    private void validateOrderId(final UUID orderId) throws PancakeServiceException {
        if (orderId == null) {
            throw new PancakeServiceException(ORDER_CANNOT_BE_PROCESSED_WITHOUT_ORDER_ID);
        }
    }

    private void validateDeliveryInfo(final DeliveryInfo deliveryInfo) throws PancakeServiceException {
        if (deliveryInfo == null) {
            throw new PancakeServiceException(ORDER_DETAILS_SHOULD_NOT_BE_NULL);
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
}