package org.pancakelab.service;

import org.pancakelab.model.OrderDetails;

import java.util.List;
import java.util.UUID;

public interface DeliveryService {
    List<OrderDetails> viewCompletedOrders();
    void acceptOrder(UUID orderId);
    void sendForTheDelivery(UUID orderId);
}
