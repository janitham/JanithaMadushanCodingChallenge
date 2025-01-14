package org.pancakelab.service;

import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.OrderStatus;

import java.util.UUID;
import java.util.concurrent.*;

public class DeliveryService {

    private final ExecutorService deliveryPartnerPool;
    private final BlockingDeque<UUID> deliveryQueue;
    private final ConcurrentMap<UUID, OrderDetails> orders;
    private final ConcurrentMap<UUID, OrderStatus> orderStatus;

    public DeliveryService(
            final int numberOfDeliveryPartners,
            final BlockingDeque<UUID> deliveryQueue,
            final ConcurrentMap<UUID, OrderDetails> orders,
            final ConcurrentMap<UUID, OrderStatus> orderStatus
    ) {
        this.deliveryQueue = deliveryQueue;
        this.orders = orders;
        this.orderStatus = orderStatus;
        this.deliveryPartnerPool = Executors.newFixedThreadPool(numberOfDeliveryPartners);
        initializeDeliveryPartners(numberOfDeliveryPartners);
    }

    private void initializeDeliveryPartners(int numberOfDeliveryPartners) {
        for (int i = 0; i < numberOfDeliveryPartners; i++) {
            deliveryPartnerPool.submit(new DeliveryPartnerImpl(orders, deliveryQueue, orderStatus));
        }
    }

    public void shutdown() {
        deliveryPartnerPool.shutdown();
        try {
            if (!deliveryPartnerPool.awaitTermination(60, TimeUnit.SECONDS)) {
                deliveryPartnerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            deliveryPartnerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}