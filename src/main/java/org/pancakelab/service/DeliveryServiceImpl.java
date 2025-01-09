package org.pancakelab.service;

import org.pancakelab.model.OrderDetails;

import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class DeliveryServiceImpl implements DeliveryService, Runnable {

    private final Logger logger;
    private final ConcurrentMap<UUID, OrderDetails> orders;
    private final BlockingDeque<UUID> deliveryQueue;

    public DeliveryServiceImpl(
            final ConcurrentMap<UUID, OrderDetails> orders,
            final BlockingDeque<UUID> deliveryQueue,
            final Logger logger
    ) {
        this.orders = orders;
        this.deliveryQueue = deliveryQueue;
        this.logger = logger;
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

    private void deliverOrder(UUID orderId) {
        OrderDetails orderDetails = orders.get(orderId);
        if (orderDetails != null) {
            orderDetails.processDelivery(orders, logger);
        } else {
            logger.warning("Order not found: " + orderId);
        }
    }
}