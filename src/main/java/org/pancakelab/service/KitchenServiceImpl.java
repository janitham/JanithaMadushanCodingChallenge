package org.pancakelab.service;

import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderStatus;
import org.pancakelab.model.PancakeRecipe;
import org.pancakelab.model.User;
import org.pancakelab.util.PancakeFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class KitchenServiceImpl implements KitchenService {
    private final ConcurrentHashMap<UUID, OrderDetails> orders;
    private final ConcurrentHashMap<UUID, OrderStatus> orderStatus;
    private final ExecutorService executorService;
    private final BlockingDeque<UUID> orderQueue;
    private final BlockingDeque<UUID> deliveryQueue;
    private final Map<UUID, Map<PancakeRecipe, Integer>> localOrderMap;

    public KitchenServiceImpl(
            final ConcurrentHashMap<UUID, OrderDetails> orders,
            final ConcurrentHashMap<UUID, OrderStatus> orderStatus,
            final BlockingDeque<UUID> orderQueue, BlockingDeque<UUID> deliveryQueue
    ) {
        this.orders = orders;
        this.orderStatus = orderStatus;
        this.orderQueue = orderQueue;
        this.deliveryQueue = deliveryQueue;
        this.executorService = Executors.newFixedThreadPool(10);
        this.localOrderMap = new ConcurrentHashMap<>();
        startOrderUpdateThread();
    }

    private void startOrderUpdateThread() {
        executorService.submit(() -> {
            while (true) {
                try {
                    UUID orderId = orderQueue.take();
                    updateLocalOrderMap(orderId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    private void updateLocalOrderMap(UUID orderId) {
        OrderDetails orderDetails = orders.get(orderId);
        if (orderDetails != null) {
            Map<PancakeRecipe, Integer> pancakeRecipes = new ConcurrentHashMap<>();
            orderDetails.getPancakes().forEach((pancake, quantity) -> {
                PancakeRecipe recipe = PancakeFactory.get(pancake);
                pancakeRecipes.put(recipe, quantity);
            });
            localOrderMap.put(orderId, pancakeRecipes);
        }
    }

    @Override
    public Map<UUID, Map<PancakeRecipe, Integer>> viewOrders(User user) {
        return new ConcurrentHashMap<>(localOrderMap);
    }

    @Override
    public void acceptOrder(User user, UUID orderId) {
        CompletableFuture.runAsync(() -> {
            OrderDetails orderDetails = orders.get(orderId);
            if (orderDetails != null) {
                synchronized (orderStatus) {
                    orderStatus.put(orderId, OrderStatus.IN_PROGRESS);
                }
            }
        }, executorService);
    }

    @Override
    public void notifyOrderCompletion(User user, UUID orderId) {
        CompletableFuture.runAsync(() -> {
            OrderDetails orderDetails = orders.get(orderId);
            if (orderDetails != null) {
                orderStatus.put(orderId, OrderStatus.READY_FOR_DELIVERY);
                deliveryQueue.add(orderId);
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