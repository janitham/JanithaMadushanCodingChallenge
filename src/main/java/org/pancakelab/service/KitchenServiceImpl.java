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

    private KitchenServiceImpl(
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
        return deliveryExecutor.submit(new PreparationTask(deliveryQueue, orders, orderId));
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