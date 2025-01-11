package org.pancakelab.service;

import org.pancakelab.model.ORDER_STATUS;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderInfo;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

public class OrderServiceImpl implements OrderService {

    private final KitchenService kitchenService;
    private final ConcurrentMap<UUID, OrderInfo> orders;
    public static String ORDER_DETAILS_SHOULD_NOT_BE_NULL = "Order details cannot be null";
    public static String ORDER_CANNOT_BE_OPENED_WITH_THE_SAME_ORDER_ID = "Cannot open order with the same order ID";
    public static String ORDER_NOT_FOUND = "Order not found";
    public static String ORDER_CANNNOT_BE_PROCESSED_WITHOUT_ORDER_ID = "Order cannot be processed without order ID";

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
            throw new IllegalArgumentException(ORDER_CANNOT_BE_OPENED_WITH_THE_SAME_ORDER_ID);
        } else if (!shouldNotExist && !orders.containsKey(orderId)) {
            throw new IllegalArgumentException(ORDER_NOT_FOUND);
        }
    }
}