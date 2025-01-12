package org.pancakelab.service;

import org.pancakelab.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

public class OrderServiceImpl implements OrderService {

    public static final String ORDER_DETAILS_SHOULD_NOT_BE_NULL = "Order details cannot be null";
    public static final String ORDER_NOT_FOUND = "Order not found";
    public static final String ORDER_CANNOT_BE_PROCESSED_WITHOUT_ORDER_ID = "Order id cannot be null";
    public static final String DUPLICATE_ORDERS_CANNOT_BE_PLACED = "Cannot create an order for the same delivery location";

    private final KitchenService kitchenService;
    private final ConcurrentMap<UUID, OrderInfo> orders;
    private final ConcurrentHashMap<DeliveryInfo, UUID> orderStorage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Map<PancakeMenu, Integer>> orderItems = new ConcurrentHashMap<>();

    public OrderServiceImpl(final KitchenService kitchenService, final ConcurrentMap<UUID, OrderInfo> orders) {
        this.kitchenService = kitchenService;
        this.orders = orders;
    }

    @Override
    public UUID createOrder(final DeliveryInfo deliveryInformation) throws PancakeServiceException {
        validateDeliveryInfo(deliveryInformation);
        var orderId = UUID.randomUUID();
        if (orderStorage.putIfAbsent(deliveryInformation, orderId) != null) {
            throw new PancakeServiceException(DUPLICATE_ORDERS_CANNOT_BE_PLACED);
        }
        return orderId;
    }

    @Override
    public void addPancakes(final UUID orderId, final Map<PancakeMenu, Integer> pancakes) {
        validateOrderId(orderId);
        if (!orderStorage.containsValue(orderId)) {
            throw new IllegalStateException(ORDER_NOT_FOUND);
        }
        orderItems.merge(orderId, new HashMap<>(pancakes), (oldPancakes, newPancakes) -> {
            newPancakes.forEach((type, count) -> oldPancakes.merge(type, count, Integer::sum));
            return oldPancakes;
        });
    }

    @Override
    public Map<PancakeMenu, Integer> orderSummary(final UUID orderId) {
        validateOrderId(orderId);
        final Map<PancakeMenu, Integer> items = orderItems.get(orderId);
        if (items == null) {
            throw new IllegalStateException(ORDER_NOT_FOUND);
        }
        return new HashMap<>(items);
    }

    @Override
    public Future<ORDER_STATUS> complete(final UUID orderId) {
        validateOrderId(orderId);
        if (!orderStorage.containsValue(orderId)) {
            throw new IllegalStateException(ORDER_NOT_FOUND);
        }
        var deliveryInfo = getDeliveryInfoByOrderId(orderId);
        var orderDetails = new OrderDetails.Builder()
                .withDeliveryInfo(deliveryInfo)
                .withOrderId(orderId)
                .withPanCakes(orderItems.get(orderId))
                .build();
        orders.put(orderId, new OrderInfo(orderDetails, ORDER_STATUS.PENDING));
        cleanUpOrder(orderId, deliveryInfo);
        return kitchenService.processOrder(orderId);
    }

    @Override
    public void cancel(final UUID orderId) {
        validateOrderId(orderId);
        if (!orderStorage.containsValue(orderId)) {
            throw new IllegalStateException(ORDER_NOT_FOUND);
        }
        var deliveryInfo = getDeliveryInfoByOrderId(orderId);
        cleanUpOrder(orderId, deliveryInfo);
    }

    private void validateOrderId(final UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException(ORDER_CANNOT_BE_PROCESSED_WITHOUT_ORDER_ID);
        }
    }

    private void validateDeliveryInfo(final DeliveryInfo deliveryInfo) {
        if (deliveryInfo == null) {
            throw new IllegalArgumentException(ORDER_DETAILS_SHOULD_NOT_BE_NULL);
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