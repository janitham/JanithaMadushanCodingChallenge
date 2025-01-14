package org.pancakelab.service;

import org.pancakelab.model.OrderStatus;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.tasks.PreparationTask;

import java.util.UUID;
import java.util.concurrent.*;

public class KitchenService {
    private static KitchenService instance;
    private final ExecutorService deliveryExecutor;
    private final BlockingDeque<UUID> deliveryQueue;
    private final ConcurrentMap<UUID, OrderDetails> orders;
    private final ConcurrentHashMap<UUID, OrderStatus> orderStatus;

    public KitchenService(
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

    public void submitTask(PreparationTask preparationTask){
        deliveryExecutor.submit(preparationTask);
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