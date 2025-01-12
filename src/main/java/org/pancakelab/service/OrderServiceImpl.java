package org.pancakelab.service;

import org.pancakelab.model.*;
import org.pancakelab.util.PancakeFactoryMenu;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

public class OrderServiceImpl implements OrderService {

    public static String ORDER_DETAILS_SHOULD_NOT_BE_NULL = "Order details cannot be null";
    public static String ORDER_CANNOT_BE_OPENED_WITH_THE_SAME_ORDER_ID = "Cannot open order with the same order ID";
    public static String ORDER_NOT_FOUND = "Order not found";
    public static String ORDER_CANNNOT_BE_PROCESSED_WITHOUT_ORDER_ID = "Order cannot be processed without order ID";
    public static String DUPLICATE_ORDERS_CANNOT_BE_PLACED = "Can not create an order for the same delivery location";

    private final KitchenService kitchenService;
    private final ConcurrentMap<UUID, OrderInfo> orders;

    private final ConcurrentHashMap<DeliveryInfo, UUID> orderStorage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Map<PancakeFactoryMenu.PANCAKE_TYPE, Integer>> orderItems = new ConcurrentHashMap<>();

    public OrderServiceImpl(
            final KitchenService kitchenService,
            final ConcurrentMap<UUID, OrderInfo> orders
    ) {
        this.kitchenService = kitchenService;
        this.orders = orders;
    }

    @Override
    public UUID open(OrderDetails orderDetails) {
        validateOrderDetails(orderDetails);
        UUID orderId = orderDetails.getOrderId();
        checkOrderExistence(orderId, true);
        orders.put(orderId, new OrderInfo(orderDetails, ORDER_STATUS.PENDING));
        return orderId;
    }

    @Override
    public void cancel(UUID orderId) {
        validateOrderId(orderId);
        checkOrderExistence(orderId, false);
        orders.remove(orderId);
    }

    @Override
    public Future<ORDER_STATUS> complete(UUID orderId) {
        validateOrderId(orderId);
        checkOrderExistence(orderId, false);
        return kitchenService.processOrder(orderId);
    }

    @Override
    public UUID createOrder(final DeliveryInfo deliveryInformation) throws PancakeServiceException {
        // validate if the delivery information is correct
        var orderId = UUID.randomUUID();
        if (orderStorage.putIfAbsent(deliveryInformation, orderId) != null) {
            throw new PancakeServiceException(DUPLICATE_ORDERS_CANNOT_BE_PLACED);
        }
        return orderId;
    }

    @Override
    public void addPancakes(UUID orderId, Map<PancakeFactoryMenu.PANCAKE_TYPE, Integer> pancakes) {
        validateOrderId(orderId);
        if(!orderStorage.containsValue(orderId)){
            throw new IllegalStateException(ORDER_NOT_FOUND);
        }
        orderItems.merge(orderId, pancakes, (oldPancakes, newPancakes) -> {
            newPancakes.forEach((type, count) ->
                    oldPancakes.merge(type, count, Integer::sum)
            );
            return oldPancakes;
        });
    }

    @Override
    public Map<PancakeFactoryMenu.PANCAKE_TYPE, Integer> orderSummary(UUID orderId) {
        validateOrderId(orderId);
        final Map<PancakeFactoryMenu.PANCAKE_TYPE, Integer> items = orderItems.get(orderId);
        if (items == null) {
            throw new IllegalStateException(ORDER_NOT_FOUND);
        }
        return new HashMap<>(items);
    }

    private void validateOrderDetails(OrderDetails orderDetails) {
        if (orderDetails == null) {
            throw new IllegalArgumentException(ORDER_DETAILS_SHOULD_NOT_BE_NULL);
        }
    }

    private void validateOrderId(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException(ORDER_CANNNOT_BE_PROCESSED_WITHOUT_ORDER_ID);
        }
    }

    private void checkOrderExistence(UUID orderId, boolean shouldNotExist) {
        if (shouldNotExist && orders.containsKey(orderId)) {
            throw new IllegalStateException(ORDER_CANNOT_BE_OPENED_WITH_THE_SAME_ORDER_ID);
        } else if (!shouldNotExist && !orders.containsKey(orderId)) {
            throw new IllegalStateException(ORDER_NOT_FOUND);
        }
    }
}