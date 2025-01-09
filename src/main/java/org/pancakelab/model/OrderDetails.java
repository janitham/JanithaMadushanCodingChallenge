package org.pancakelab.model;

import org.pancakelab.model.pancakes.PancakeRecipe;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.UUID;
import java.util.logging.Logger;

public class OrderDetails {
    private DeliveryInfo deliveryInfo;
    private List<Pancake> pancakes;
    private UUID orderId = UUID.randomUUID();
    private STATUS status = STATUS.PENDING;

    public UUID getOrderId() {
        return orderId;
    }

    public boolean canBeDelivered() {
        return status == STATUS.PENDING;
    }

    public void markAsDelivered() {
        status = STATUS.DELIVERED;
    }

    public void processDelivery(ConcurrentMap<UUID, OrderDetails> orders, Logger logger) {
        if (canBeDelivered()) {
            markAsDelivered();
            orders.remove(orderId);
            logger.info("Delivering order: " + orderId);
        } else {
            logger.warning("Order cannot be delivered: " + orderId);
        }
    }

    @Override
    public String toString() {
        return "OrderDetails{" +
                "deliveryInfo=" + deliveryInfo +
                ", pancakes=" + pancakes +
                ", status=" + status +
                '}';
    }

    public enum STATUS {
        PENDING,
        COMPLETED,
        DELIVERED,
    }
}
