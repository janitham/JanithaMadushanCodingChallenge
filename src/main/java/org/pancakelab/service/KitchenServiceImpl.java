package org.pancakelab.service;

import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderStatus;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class KitchenServiceImpl implements KitchenService {
    private final ConcurrentMap<UUID, OrderDetails> orders;
    private final ConcurrentHashMap<UUID, OrderStatus> orderStatus;
    private final ExecutorService executorService;

    public KitchenServiceImpl(
            final ConcurrentMap<UUID, OrderDetails> orders,
            final ConcurrentHashMap<UUID, OrderStatus> orderStatus
    ) {
        this.orders = orders;
        this.orderStatus = orderStatus;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @Override
    public List<OrderDetails> viewOrders() {
        return List.copyOf(orders.values());
    }

    @Override
    public void acceptOrder(UUID orderId) {
        CompletableFuture.runAsync(() -> {
            OrderDetails orderDetails = orders.get(orderId);
            if (orderDetails != null) {
                orderStatus.put(orderId, OrderStatus.IN_PROGRESS);
            }
        }, executorService);
    }

    @Override
    public void notifyOrderCompletion(UUID orderId) {
        CompletableFuture.runAsync(() -> {
            OrderDetails orderDetails = orders.get(orderId);
            if (orderDetails != null) {
                orderStatus.put(orderId, OrderStatus.READY_FOR_DELIVERY);
            }
        }, executorService);
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}