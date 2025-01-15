package org.pancakelab.service;

import org.pancakelab.model.DeliveryInfo;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderStatus;
import org.pancakelab.model.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class DeliveryServiceImpl implements DeliveryService {
    private final ConcurrentHashMap<UUID, OrderDetails> orders;
    private final ConcurrentHashMap<UUID, OrderStatus> orderStatus;
    private final ExecutorService executorService;
    private final BlockingDeque<UUID> deliveryQueue;
    private final Map<UUID, DeliveryInfo> localDeliveryMap = new ConcurrentHashMap<>();

    public DeliveryServiceImpl(
            final ConcurrentHashMap<UUID, OrderDetails> orders,
            final ConcurrentHashMap<UUID, OrderStatus> orderStatus, BlockingDeque<UUID> deliveryQueue
    ) {
        this.orders = orders;
        this.orderStatus = orderStatus;
        this.deliveryQueue = deliveryQueue;
        this.executorService = Executors.newFixedThreadPool(10);
        startOrderUpdateThread();
    }

    @Override
    public synchronized Map<UUID, DeliveryInfo> viewCompletedOrders(User user) {
        return localDeliveryMap;
    }

    private void startOrderUpdateThread() {
        executorService.submit(() -> {
            while (true) {
                try {
                    final UUID orderId = deliveryQueue.take();
                    OrderDetails orderDetails = orders.get(orderId);
                    if (orderDetails != null) {
                        localDeliveryMap.put(orderId, orderDetails.getDeliveryInfo());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
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