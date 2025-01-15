package org.pancakelab.service;

import org.pancakelab.model.DeliveryInfo;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderStatus;
import org.pancakelab.model.User;
import org.pancakelab.util.PancakeUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Delivery service API can be used to accept and deliver orders.
 * To improve the performance, the delivery service uses a separate thread to update the order status.
 * The delivery service uses a blocking queue to store the order ids that are ready for delivery.
 * This service tries to only show the delivery information and order id to the user for security reasons.
 */
public class DeliveryServiceImpl implements DeliveryService {
    private final ConcurrentMap<UUID, OrderDetails> orders;
    private final ConcurrentMap<UUID, OrderStatus> orderStatus;
    private final ExecutorService executorService;
    private final BlockingDeque<UUID> deliveryQueue;
    private final Map<UUID, DeliveryInfo> localDeliveryMap = new ConcurrentHashMap<>();

    /**
     * Constructs a new DeliveryServiceImpl.
     *
     * @param orders          the map of order details
     * @param orderStatus     the map of order statuses
     * @param deliveryQueue   the queue of orders ready for delivery
     * @param internalThreads the number of internal threads to use
     */
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

    /**
     * Allows the user to view completed orders. The orders view is pre-processed asynchronously,
     * providing the outcome efficiently without waiting for processing.
     *
     * @param user the user requesting to view completed orders
     * @return a map of order IDs to delivery information
     */
    @Override
    public synchronized Map<UUID, DeliveryInfo> viewCompletedOrders(User user) {
        return localDeliveryMap;
    }

    /**
     * Allows the user to accept an order in a non-blocking manner. The order status is updated
     * asynchronously, and the user is notified about the order status efficiently without waiting for processing.
     *
     * @param user    the user accepting the order
     * @param orderId the ID of the order to be accepted
     */
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

    /**
     * Allows the user to send the order for delivery in a non-blocking manner. The order status is updated
     * asynchronously, and the user is notified about the order status efficiently without waiting for processing.
     *
     * @param user    the user sending the order for delivery
     * @param orderId the ID of the order to be delivered
     */
    @Override
    public void sendForTheDelivery(User user, UUID orderId) {
        CompletableFuture.runAsync(() -> {
            OrderDetails orderDetails;
            synchronized (orders) {
                orderDetails = orders.get(orderId);
            }
            if (orderDetails != null) {
                synchronized (orderStatus) {
                    if (orderStatus.get(orderId) == OrderStatus.OUT_FOR_DELIVERY) {
                        orderStatus.put(orderId, OrderStatus.DELIVERED);
                    }
                }
                synchronized (localDeliveryMap) {
                    localDeliveryMap.remove(orderId);
                }
                synchronized (orders) {
                    orders.remove(orderId);
                }
            }
            PancakeUtils.notifyUser(user, OrderStatus.DELIVERED);
        }, executorService);
    }

    /**
     * Starts a thread to update the local delivery map with orders from the delivery queue.
     */
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