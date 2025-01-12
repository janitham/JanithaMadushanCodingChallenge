package org.pancakelab.service;

import org.pancakelab.model.ORDER_STATUS;
import org.pancakelab.model.OrderDetails;

import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class PreparationTask implements Callable<ORDER_STATUS> {

    private final BlockingDeque<UUID> deliveryQueue;
    private final ConcurrentMap<UUID, OrderDetails> orders;
    private final Logger logger = Logger.getLogger(PreparationTask.class.getName());
    private final UUID orderId;

    public PreparationTask(
            final BlockingDeque<UUID> deliveryQueue,
            final ConcurrentMap<UUID, OrderDetails> orders,
            final UUID orderId
    ) {
        this.deliveryQueue = deliveryQueue;
        this.orders = orders;
        this.orderId = orderId;
    }

    @Override
    public ORDER_STATUS call() {
        try {
            prepareOrder();
            return processOrder();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ORDER_STATUS.ERROR;
        }
    }

    private void prepareOrder() throws InterruptedException {
        Thread.sleep(1000);
    }

    private ORDER_STATUS processOrder() throws InterruptedException {
        final OrderDetails orderInfo = orders.get(orderId);
        if (orderInfo == null) {
            logger.warning("Order not found: %s".formatted(orderId));
            return ORDER_STATUS.NOT_FOUND;
        } else {
            logger.info("Order is ready for delivery: %s".formatted(orderId));
            deliveryQueue.put(orderId);
            return ORDER_STATUS.READY_FOR_DELIVERY;
        }
    }
}
