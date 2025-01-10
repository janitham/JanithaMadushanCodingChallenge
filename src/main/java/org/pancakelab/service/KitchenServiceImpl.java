package org.pancakelab.service;

import org.pancakelab.model.ORDER_STATUS;
import org.pancakelab.model.OrderInfo;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class KitchenServiceImpl implements KitchenService {
    private static KitchenServiceImpl instance;
    private final ExecutorService deliveryExecutor;
    private final BlockingDeque<UUID> deliveryQueue;
    private final ConcurrentMap<UUID, OrderInfo> orders;
    private final Logger logger = Logger.getLogger(KitchenServiceImpl.class.getName());

    KitchenServiceImpl(
            final int numberOfChefsInTheKitchen,
            final BlockingDeque<UUID> deliveryQueue,
            final ConcurrentMap<UUID, OrderInfo> orders
    ) {
        this.deliveryExecutor = Executors.newFixedThreadPool(numberOfChefsInTheKitchen);
        this.deliveryQueue = deliveryQueue;
        this.orders = orders;
    }

    public static synchronized KitchenServiceImpl getInstance(
            final int numberOfChefsInTheKitchen,
            final BlockingDeque<UUID> deliveryQueue,
            final ConcurrentMap<UUID, OrderInfo> orders
    ) {
        if (instance == null) {
            instance = new KitchenServiceImpl(numberOfChefsInTheKitchen, deliveryQueue, orders);
        }
        return instance;
    }

    @Override
    public Future<ORDER_STATUS> processOrder(UUID orderId) {
        return deliveryExecutor.submit(() -> {
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
        });
    }

    public void shutdown() {
        deliveryExecutor.shutdown();
        try {
            if (!deliveryExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                deliveryExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            deliveryExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}