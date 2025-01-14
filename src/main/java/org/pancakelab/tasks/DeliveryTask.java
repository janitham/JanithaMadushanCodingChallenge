package org.pancakelab.tasks;

import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderStatus;
import org.pancakelab.util.PancakeUtils;

import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class DeliveryTask implements Runnable {

    private final Logger logger = Logger.getLogger(DeliveryTask.class.getName());
    private final ConcurrentMap<UUID, OrderDetails> orders;
    private final BlockingDeque<UUID> deliveryQueue;
    private final ConcurrentMap<UUID, OrderStatus> orderStatus;

    public DeliveryTask(
            final ConcurrentMap<UUID, OrderDetails> orders,
            final BlockingDeque<UUID> deliveryQueue,
            final ConcurrentMap<UUID, OrderStatus> orderStatus
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
            orderStatus.put(orderId, OrderStatus.DELIVERED);
            PancakeUtils.notifyUser(orderDetails.getUser(), OrderStatus.DELIVERED);
        } else {
            handleMissingOrder(orderId);
        }
    }

    private void handleMissingOrder(final UUID orderId) {
        orderStatus.put(orderId, OrderStatus.ERROR);
        logger.warning("Order not found: %s".formatted(orderId));
    }
}