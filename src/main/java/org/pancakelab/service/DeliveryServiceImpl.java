package org.pancakelab.service;

import org.pancakelab.model.ORDER_STATUS;
import org.pancakelab.model.OrderDetails;

import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class DeliveryServiceImpl implements DeliveryService, Runnable {

    private final Logger logger = Logger.getLogger(DeliveryServiceImpl.class.getName());
    private final ConcurrentMap<UUID, OrderDetails> orders;
    private final BlockingDeque<UUID> deliveryQueue;
    private final ConcurrentMap<UUID, ORDER_STATUS> orderStatus;

    public DeliveryServiceImpl(
            final ConcurrentMap<UUID, OrderDetails> orders,
            final BlockingDeque<UUID> deliveryQueue,
            final ConcurrentMap<UUID, ORDER_STATUS> orderStatus
    ) {
        this.orders = orders;
        this.deliveryQueue = deliveryQueue;
        this.orderStatus = orderStatus;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                deliverOrder(deliveryQueue.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void deliverOrder(final UUID orderId) {
        final OrderDetails orderDetails = orders.get(orderId);
        if (orderDetails != null) {
            orders.remove(orderId);
            logger.info("Delivering order: %s".formatted(orderDetails.getOrderId()));
            orderStatus.put(orderId, ORDER_STATUS.DELIVERED);
        } else {
            logger.warning("Order not found: %s".formatted(orderId));
        }
    }
}