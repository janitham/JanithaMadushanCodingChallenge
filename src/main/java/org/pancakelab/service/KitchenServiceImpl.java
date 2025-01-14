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

    public KitchenServiceImpl(
            final ConcurrentHashMap<UUID, OrderDetails> orders,
            final ConcurrentHashMap<UUID, OrderStatus> orderStatus
    ) {
        this.orders = orders;
        this.orderStatus = orderStatus;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @Override
    public Map<UUID, Map<PancakeRecipe, Integer>> viewOrders(User user) {
        Map<UUID, Map<PancakeRecipe, Integer>> result = new ConcurrentHashMap<>();
        for (OrderDetails order : orders.values()) {
            Map<PancakeRecipe, Integer> pancakeRecipes = new ConcurrentHashMap<>();
            order.getPancakes().forEach((pancake, quantity) -> {
                PancakeRecipe recipe = PancakeFactory.get(pancake);
                pancakeRecipes.put(recipe, quantity);
            });
            result.put(order.getOrderId(), pancakeRecipes);
        }
        return result;
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