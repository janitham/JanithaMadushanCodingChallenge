package org.pancakelab.service;

import org.pancakelab.model.OrderDetails;

import java.util.List;
import java.util.UUID;

public interface KitchenService {
    List<OrderDetails> viewOrders();
    void acceptOrder(UUID orderId);
    void notifyOrderCompletion(UUID orderId);
}
