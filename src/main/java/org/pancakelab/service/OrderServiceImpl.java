package org.pancakelab.service;

import org.pancakelab.model.ORDER_STATUS;
import org.pancakelab.model.OrderDetails;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

public class OrderServiceImpl implements OrderService {

    private final KitchenService kitchenService;
    private final ConcurrentMap<UUID, OrderDetails> orders;

    public OrderServiceImpl(
            final KitchenService kitchenService,
            final ConcurrentMap<UUID, OrderDetails> orders
    ) {
        this.kitchenService = kitchenService;
        this.orders = orders;
    }

    @Override
    public UUID open(OrderDetails orderDetails) {
        validateOrderDetails(orderDetails);
        UUID orderId = orderDetails.getOrderId();
        checkOrderExistence(orderId, true);
        orders.put(orderId, orderDetails);
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
            throw new IllegalArgumentException("Order cannot be opened without details");
        }
    }

    private void validateOrderId(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order cannot be processed without order ID");
        }
    }

    private void checkOrderExistence(UUID orderId, boolean shouldNotExist) {
        if (shouldNotExist && orders.containsKey(orderId)) {
            throw new IllegalArgumentException("Cannot open order with the same order ID");
        } else if (!shouldNotExist && !orders.containsKey(orderId)) {
            throw new IllegalArgumentException("Order not found");
        }
    }
}