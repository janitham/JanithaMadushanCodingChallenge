package org.pancakelab.service;

import org.pancakelab.model.ORDER_STATUS;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.util.PancakeUtils;

import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import java.util.stream.IntStream;

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
            return processOrder();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ORDER_STATUS.ERROR;
        }
    }

    private void prepareOrder(OrderDetails orderDetails) {
        orderDetails.getPancakes().forEach(
                (pancake, quantity) -> IntStream.range(0, quantity)
                        .forEach(i -> PancakeUtils.preparePancake(pancake)));
    }

    private ORDER_STATUS processOrder() throws InterruptedException {
        final OrderDetails orderDetails = orders.get(orderId);
        if (orderDetails == null) {
            logger.warning("Order not found: %s".formatted(orderId));
            return ORDER_STATUS.NOT_FOUND;
        } else {
            prepareOrder(orderDetails);
            logger.info("Order is ready for delivery: %s".formatted(orderId));
            deliveryQueue.put(orderId);
            return ORDER_STATUS.READY_FOR_DELIVERY;
        }
    }
}
