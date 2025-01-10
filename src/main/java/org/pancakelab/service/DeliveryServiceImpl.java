package org.pancakelab.service;

import org.pancakelab.model.ORDER_STATUS;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderInfo;

import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class DeliveryServiceImpl implements DeliveryService, Runnable {

    private final Logger logger;
    private final ConcurrentMap<UUID, OrderInfo> orders;
    private final BlockingDeque<UUID> deliveryQueue;

    public DeliveryServiceImpl(
            final ConcurrentMap<UUID, OrderInfo> orders,
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
        final OrderInfo orderInfo = orders.get(orderId);
        if (orderInfo != null) {
            if (orderInfo.getStatus() == ORDER_STATUS.PENDING) {
                OrderDetails orderDetails = orderInfo.getOrderDetails();
                orders.remove(orderId);
                logger.info("Delivering order: " + orderDetails.getOrderId());
            } else {
                logger.warning("Invalid Status: " + orderId);
            }
        } else {
            logger.warning("Order not found: " + orderId);
        }
    }
}