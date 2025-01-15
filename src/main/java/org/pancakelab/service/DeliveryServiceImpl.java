package org.pancakelab.service;

import org.pancakelab.model.DeliveryInfo;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderStatus;
import org.pancakelab.model.User;
import org.pancakelab.util.PancakeUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class DeliveryServiceImpl implements DeliveryService {
    private final ConcurrentMap<UUID, OrderDetails> orders;
    private final ConcurrentMap<UUID, OrderStatus> orderStatus;
    private final ExecutorService executorService;
    private final BlockingDeque<UUID> deliveryQueue;
    private final Map<UUID, DeliveryInfo> localDeliveryMap = new ConcurrentHashMap<>();

    public DeliveryServiceImpl(
            final ConcurrentMap<UUID, OrderDetails> orders,
            final ConcurrentMap<UUID, OrderStatus> orderStatus, BlockingDeque<UUID> deliveryQueue,
            final Integer internalThreads
    ) {
        this.orders = orders;
        this.orderStatus = orderStatus;
        this.deliveryQueue = deliveryQueue;
        this.executorService = Executors.newFixedThreadPool(internalThreads);
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
                    OrderDetails orderDetails;
                    synchronized (orders) {
                        orderDetails = orders.get(orderId);
                    }
                    if (orderDetails != null) {
                        synchronized (localDeliveryMap) {
                            localDeliveryMap.put(orderId, orderDetails.getDeliveryInfo());
                        }
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
            final OrderDetails orderDetails = orders.get(orderId);
            if (orderDetails != null) {
                synchronized (orderStatus) {
                    if (orderStatus.get(orderId) == OrderStatus.READY_FOR_DELIVERY) {
                        orderStatus.put(orderId, OrderStatus.OUT_FOR_DELIVERY);
                        PancakeUtils.notifyUser(user, OrderStatus.OUT_FOR_DELIVERY);
                    }
                }
            }
        }, executorService);
    }

    @Override
    public void sendForTheDelivery(User user, UUID orderId) {
        CompletableFuture.runAsync(() -> {
            synchronized (orderStatus) {
                OrderDetails orderDetails = orders.get(orderId);
                if (orderDetails != null && orderStatus.get(orderId) == OrderStatus.OUT_FOR_DELIVERY) {
                    orderStatus.put(orderId, OrderStatus.DELIVERED);
                    synchronized (localDeliveryMap) {
                        localDeliveryMap.remove(orderId);
                    }
                    synchronized (orders) {
                        orders.remove(orderId);
                    }
                }
            }
            PancakeUtils.notifyUser(user, OrderStatus.DELIVERED);
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