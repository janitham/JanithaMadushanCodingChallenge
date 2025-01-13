package org.pancakelab.service;

import org.pancakelab.model.OrderStatus;
import org.pancakelab.model.OrderDetails;

import java.util.UUID;
import java.util.concurrent.*;

public class KitchenServiceImpl implements KitchenService {
    private static KitchenServiceImpl instance;
    private final ExecutorService deliveryExecutor;
    private final BlockingDeque<UUID> deliveryQueue;
    private final ConcurrentMap<UUID, OrderDetails> orders;
    private final ConcurrentHashMap<UUID, OrderStatus> orderStatus;

    private KitchenServiceImpl(
            final BlockingDeque<UUID> deliveryQueue,
            final ConcurrentMap<UUID, OrderDetails> orders,
            final ExecutorService executorService,
            final ConcurrentHashMap<UUID, OrderStatus> orderStatus
    ) {
        this.deliveryQueue = deliveryQueue;
        this.orders = orders;
        this.deliveryExecutor = executorService;
        this.orderStatus = orderStatus;
    }

    public static synchronized KitchenServiceImpl getInstance(
            final int numberOfChefsInTheKitchen,
            final BlockingDeque<UUID> deliveryQueue,
            final ConcurrentMap<UUID, OrderDetails> orders,
            final ConcurrentHashMap<UUID, OrderStatus> orderStatus
    ) {
        if (instance == null) {
            instance = new KitchenServiceImpl(
                    deliveryQueue,
                    orders,
                    Executors.newFixedThreadPool(numberOfChefsInTheKitchen),
                    orderStatus
            );
        }
        return instance;
    }

    public static synchronized KitchenServiceImpl getInstance(
            final BlockingDeque<UUID> deliveryQueue,
            final ConcurrentMap<UUID, OrderDetails> orders,
            final ExecutorService deliveryExecutor,
            final ConcurrentHashMap<UUID, OrderStatus> orderStatus
    ) {
        if (instance == null) {
            instance = new KitchenServiceImpl(deliveryQueue, orders, deliveryExecutor, orderStatus);
        }
        return instance;
    }

    @Override
    public void processOrder(UUID orderId) {
        deliveryExecutor.submit(new PreparationTask(deliveryQueue, orders, orderId, orderStatus));
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