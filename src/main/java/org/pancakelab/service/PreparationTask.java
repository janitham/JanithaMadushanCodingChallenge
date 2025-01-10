package org.pancakelab.service;

import org.pancakelab.model.ORDER_STATUS;
import org.pancakelab.model.OrderInfo;

import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class PreparationTask implements Callable<ORDER_STATUS> {

    private final BlockingDeque<UUID> deliveryQueue;
    private final ConcurrentMap<UUID, OrderInfo> orders;
    private final Logger logger = Logger.getLogger(PreparationTask.class.getName());
    private final UUID orderId;

    public PreparationTask(BlockingDeque<UUID> deliveryQueue, ConcurrentMap<UUID, OrderInfo> orders, UUID orderId) {
        this.deliveryQueue = deliveryQueue;
        this.orders = orders;
        this.orderId = orderId;
    }

    @Override
    public ORDER_STATUS call() throws Exception {
        try {
            // To simulate workload
            Thread.sleep(1000);
            final OrderInfo orderInfo = orders.get(orderId);
            if (orderInfo == null) {
                logger.warning("Order not found: " + orderId);
                return ORDER_STATUS.NOT_FOUND;
            } else {
                if (ORDER_STATUS.PENDING != orderInfo.getStatus()) {
                    logger.warning("Invalid Status: " + orderId);
                    return ORDER_STATUS.INVALID;
                }
                orderInfo.setStatus(ORDER_STATUS.READY_FOR_DELIVERY);
                logger.info("Order is ready for delivery: " + orderId);
                deliveryQueue.put(orderId);
                return ORDER_STATUS.READY_FOR_DELIVERY;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ORDER_STATUS.ERROR;
        }
    }
}
