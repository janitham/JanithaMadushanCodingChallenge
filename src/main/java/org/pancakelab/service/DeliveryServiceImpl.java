package org.pancakelab.service;

import org.pancakelab.model.DeliveryInfo;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderStatus;
import org.pancakelab.model.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
    public Map<UUID, DeliveryInfo> viewCompletedOrders(User user) {
        return orders.values().stream()
                .filter(order -> orderStatus.get(order.getOrderId()) == OrderStatus.READY_FOR_DELIVERY)
                .collect(Collectors.toMap(OrderDetails::getOrderId, OrderDetails::getDeliveryInfo));
    }

    @Override
    public void acceptOrder(User user, UUID orderId) {
        CompletableFuture.runAsync(() -> {
            OrderDetails orderDetails = orders.get(orderId);
            if (orderDetails != null && orderStatus.get(orderId) == OrderStatus.READY_FOR_DELIVERY) {
                orderStatus.put(orderId, OrderStatus.OUT_FOR_DELIVERY);
            }
        }, executorService);
    }

    @Override
    public void sendForTheDelivery(User user, UUID orderId) {
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