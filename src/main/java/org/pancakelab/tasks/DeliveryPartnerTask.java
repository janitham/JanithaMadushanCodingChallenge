package org.pancakelab.tasks;

import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderStatus;
import org.pancakelab.util.PancakeUtils;

import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class DeliveryPartnerTask implements Runnable {

    private final Logger logger = Logger.getLogger(DeliveryPartnerTask.class.getName());
    private final ConcurrentMap<UUID, OrderDetails> orders;
    private final BlockingDeque<UUID> deliveryQueue;
    private final ConcurrentMap<UUID, OrderStatus> orderStatus;

    public DeliveryPartnerTask(
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
        try {
            deliverOrder(deliveryQueue.take());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warning("DeliveryTask interrupted");
        }

    }

    private void deliverOrder(final UUID orderId) throws InterruptedException {
        final OrderDetails orderDetails = orders.get(orderId);
        if (orderDetails != null) {
            if (orderStatus.get(orderId) != OrderStatus.WAITING_FOR_DELIVERY) {
                orderStatus.put(orderId, OrderStatus.ERROR);
                logger.warning("Order not ready for delivery: %s".formatted(orderId));
                return;
            }
            orders.remove(orderId);
            logger.info("Delivering order: %s".formatted(orderDetails.getOrderId()));
            PancakeUtils.notifyUser(orderDetails.getUser(), OrderStatus.DELIVERY_PARTNER_ASSIGNED);
            synchronized (orderStatus) {
                while (orderStatus.get(orderId) != OrderStatus.DELIVERED) {
                    orderStatus.wait(1000);
                }
                orderStatus.notifyAll();
            }
            logger.info("Delivered order: %s".formatted(orderDetails.getOrderId()));
            PancakeUtils.notifyUser(orderDetails.getUser(), OrderStatus.DELIVERED);
        } else {
            handleMissingOrder(orderId);
        }
    }

    private void handleMissingOrder(final UUID orderId) {
        synchronized (orderStatus) {
            orderStatus.put(orderId, OrderStatus.ERROR);
            orderStatus.notifyAll();
        }
        logger.warning("Order not found: %s".formatted(orderId));
    }
}