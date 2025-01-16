package org.pancakelab.service;

import org.pancakelab.model.DeliveryInfo;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderStatus;
import org.pancakelab.model.User;
import org.pancakelab.util.PancakeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Delivery service API can be used to accept and deliver orders.
 * To improve the performance, the delivery service uses a separate thread to update the order status.
 * The delivery service uses a blocking queue to store the order ids that are ready for delivery.
 * This service tries to only show the delivery information and order id to the user for security reasons.
 */
public class DeliveryServiceImpl implements DeliveryService {
    private final ConcurrentMap<UUID, OrderDetails> ordersRepository;
    private final ConcurrentMap<UUID, OrderStatus> orderStatusRepository;
    private final ExecutorService executorService;
    private final BlockingDeque<UUID> deliveryQueue;
    private final Map<UUID, DeliveryInfo> localDeliveryMap = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    /**
     * Constructs a new DeliveryServiceImpl.
     *
     * @param ordersRepository      the map of order details
     * @param orderStatusRepository the map of order statuses
     * @param deliveryQueue         the queue of orders ready for delivery
     * @param internalThreads       the number of internal threads to use
     */
    public DeliveryServiceImpl(
            final ConcurrentMap<UUID, OrderDetails> ordersRepository,
            final ConcurrentMap<UUID, OrderStatus> orderStatusRepository,
            final BlockingDeque<UUID> deliveryQueue,
            final Integer internalThreads
    ) {
        this.ordersRepository = ordersRepository;
        this.orderStatusRepository = orderStatusRepository;
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
        readLock.lock();
        try {
            return new HashMap<>(localDeliveryMap);
        } finally {
            readLock.unlock();
        }
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
            OrderDetails orderDetails;
            synchronized (ordersRepository) {
                orderDetails = ordersRepository.get(orderId);
            }
            if (orderDetails != null) {
                synchronized (orderStatusRepository) {
                    if (orderStatusRepository.get(orderId) == OrderStatus.READY_FOR_DELIVERY) {
                        orderStatusRepository.put(orderId, OrderStatus.OUT_FOR_DELIVERY);
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
            synchronized (ordersRepository) {
                orderDetails = ordersRepository.get(orderId);
            }
            if (orderDetails != null) {
                synchronized (orderStatusRepository) {
                    if (orderStatusRepository.get(orderId) == OrderStatus.OUT_FOR_DELIVERY) {
                        orderStatusRepository.put(orderId, OrderStatus.DELIVERED);
                    }
                }
                writeLock.lock();
                try {
                    localDeliveryMap.remove(orderId);
                } finally {
                    writeLock.unlock();
                }
                synchronized (ordersRepository) {
                    ordersRepository.remove(orderId);
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
                    synchronized (ordersRepository) {
                        orderDetails = ordersRepository.get(orderId);
                    }
                    if (orderDetails != null) {
                        writeLock.lock();
                        try {
                            localDeliveryMap.put(orderId, orderDetails.getDeliveryInfo());
                        } finally {
                            writeLock.unlock();
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