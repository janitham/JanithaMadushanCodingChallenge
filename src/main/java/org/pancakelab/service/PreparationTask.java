package org.pancakelab.service;

import org.pancakelab.model.OrderStatus;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.util.PancakeUtils;

import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class PreparationTask implements Runnable {

    private final BlockingDeque<UUID> deliveryQueue;
    private final ConcurrentMap<UUID, OrderDetails> orders;
    private final Logger logger = Logger.getLogger(PreparationTask.class.getName());
    private final UUID orderId;
    private final ConcurrentHashMap<UUID, OrderStatus> orderStatus;

    public PreparationTask(
            final BlockingDeque<UUID> deliveryQueue,
            final ConcurrentMap<UUID, OrderDetails> orders,
            final UUID orderId,
            final ConcurrentHashMap<UUID, OrderStatus> orderStatus
    ) {
        this.deliveryQueue = deliveryQueue;
        this.orders = orders;
        this.orderId = orderId;
        this.orderStatus = orderStatus;
    }

    @Override
    public void run() {
        try {
            processOrder();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            orderStatus.put(orderId, OrderStatus.ERROR);
        }
    }

    private void prepareOrder(OrderDetails orderDetails) {
        orderDetails.getPancakes().forEach(
                (pancake, quantity) -> IntStream.range(0, quantity)
                        .forEach(i -> PancakeUtils.preparePancake(pancake)));
    }

    private void processOrder() throws InterruptedException {
        final OrderDetails orderDetails = orders.get(orderId);
        if (orderDetails == null) {
            logger.warning("Order not found: %s".formatted(orderId));
            orderStatus.put(orderId, OrderStatus.ERROR);
        } else {
            prepareOrder(orderDetails);
            logger.info("Order is ready for delivery: %s".formatted(orderId));
            deliveryQueue.put(orderId);
            orderStatus.put(orderId, OrderStatus.READY_FOR_DELIVERY);
        }
    }
}
