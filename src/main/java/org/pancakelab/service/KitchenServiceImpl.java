package org.pancakelab.service;

import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderStatus;
import org.pancakelab.model.PancakeRecipe;
import org.pancakelab.model.User;
import org.pancakelab.util.PancakeFactory;
import org.pancakelab.util.PancakeUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Implementation of the KitchenService interface.
 * This service handles the processing of orders in the kitchen, including accepting orders,
 * updating their status, and notifying users upon completion.
 * It uses a separate thread to update the local order map and manages order and delivery queues.
 */
public class KitchenServiceImpl implements KitchenService {
    private final ConcurrentMap<UUID, OrderDetails> orders;
    private final ConcurrentMap<UUID, OrderStatus> orderStatus;
    private final ExecutorService executorService;
    private final BlockingDeque<UUID> orderQueue;
    private final BlockingDeque<UUID> deliveryQueue;
    private final Map<UUID, Map<PancakeRecipe, Integer>> localOrderMap;

    /**
     * Constructs a new KitchenServiceImpl.
     *
     * @param orders          the map of order details
     * @param orderStatus     the map of order statuses
     * @param orderQueue      the queue of orders to be processed
     * @param deliveryQueue   the queue of orders ready for delivery
     * @param internalThreads the number of internal threads to use
     */
    public KitchenServiceImpl(
            final ConcurrentMap<UUID, OrderDetails> orders,
            final ConcurrentMap<UUID, OrderStatus> orderStatus,
            final BlockingDeque<UUID> orderQueue,
            final BlockingDeque<UUID> deliveryQueue,
            final Integer internalThreads
    ) {
        this.orders = orders;
        this.orderStatus = orderStatus;
        this.orderQueue = orderQueue;
        this.deliveryQueue = deliveryQueue;
        this.executorService = Executors.newFixedThreadPool(internalThreads);
        this.localOrderMap = new ConcurrentHashMap<>();
        startOrderUpdateThread();
    }

    /**
     * Starts a thread to update the local order map with orders from the order queue.
     */
    private void startOrderUpdateThread() {
        executorService.submit(() -> {
            while (true) {
                try {
                    UUID orderId = orderQueue.take();
                    synchronized (localOrderMap) {
                        updateLocalOrderMap(orderId);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    /**
     * Allows the user to view the current orders.
     *
     * @param user the user requesting to view orders
     * @return a map of order IDs to pancake recipes and their quantities
     */
    @Override
    public Map<UUID, Map<PancakeRecipe, Integer>> viewOrders(User user) {
        return new ConcurrentHashMap<>(localOrderMap);
    }

    /**
     * Allows the user to accept an order. The order status is updated asynchronously.
     *
     * @param user    the user accepting the order
     * @param orderId the ID of the order to be accepted
     */
    @Override
    public void acceptOrder(User user, UUID orderId) {
        CompletableFuture.runAsync(() -> {
            OrderDetails orderDetails;
            synchronized (orders) {
                orderDetails = orders.get(orderId);
            }
            if (orderDetails != null) {
                synchronized (orderStatus) {
                    orderStatus.put(orderId, OrderStatus.IN_PROGRESS);
                }
                PancakeUtils.notifyUser(user, OrderStatus.IN_PROGRESS);
            }
        }, executorService);
    }

    /**
     * Notifies the user that the order is complete and ready for delivery. The order status is updated asynchronously.
     *
     * @param user    the user to be notified
     * @param orderId the ID of the order that is complete
     */
    @Override
    public void notifyOrderCompletion(User user, UUID orderId) {
        CompletableFuture.runAsync(() -> {
            OrderDetails orderDetails;
            synchronized (orders) {
                orderDetails = orders.get(orderId);
            }
            if (orderDetails != null) {
                synchronized (orderStatus) {
                    orderStatus.put(orderId, OrderStatus.READY_FOR_DELIVERY);
                }
                PancakeUtils.notifyUser(user, OrderStatus.READY_FOR_DELIVERY);
                synchronized (deliveryQueue) {
                    deliveryQueue.add(orderId);
                }
                synchronized (localOrderMap) {
                    localOrderMap.remove(orderId);
                }
            }
        }, executorService);
    }

    /**
     * Updates the local order map with the details of the specified order.
     *
     * @param orderId the ID of the order to be updated
     */
    private void updateLocalOrderMap(UUID orderId) {
        synchronized (orders) {
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
    }

    /**
     * Shuts down the executor service, waiting for tasks to complete or forcing shutdown if necessary.
     */
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