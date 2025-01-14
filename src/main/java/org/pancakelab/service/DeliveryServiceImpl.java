package org.pancakelab.service;

import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderStatus;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class DeliveryServiceImpl implements DeliveryService {
    private final ConcurrentHashMap<UUID, OrderDetails> orders;
    private final ConcurrentHashMap<UUID, OrderStatus> orderStatus;
    private final ExecutorService executorService;

    public DeliveryServiceImpl(
            final ConcurrentHashMap<UUID, OrderDetails> orders,
            final ConcurrentHashMap<UUID, OrderStatus> orderStatus
    ) {
        this.orders = orders;
        this.orderStatus = orderStatus;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @Override
    public List<OrderDetails> viewCompletedOrders() {
        return orders.values().stream()
                .filter(order -> orderStatus.get(order.getOrderId()) == OrderStatus.READY_FOR_DELIVERY)
                .toList();
    }

    @Override
    public void acceptOrder(UUID orderId) {
        CompletableFuture.runAsync(() -> {
            OrderDetails orderDetails = orders.get(orderId);
            if (orderDetails != null && orderStatus.get(orderId) == OrderStatus.READY_FOR_DELIVERY) {
                orderStatus.put(orderId, OrderStatus.OUT_FOR_DELIVERY);
            }
        }, executorService);
    }

    @Override
    public void sendForTheDelivery(UUID orderId) {
        CompletableFuture.runAsync(() -> {
            OrderDetails orderDetails = orders.get(orderId);
            if (orderDetails != null && orderStatus.get(orderId) == OrderStatus.OUT_FOR_DELIVERY) {
                orderStatus.put(orderId, OrderStatus.DELIVERED);
                orders.remove(orderId);
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