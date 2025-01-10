package org.pancakelab.service;

import org.pancakelab.model.ORDER_STATUS;

import java.util.UUID;
import java.util.concurrent.Future;

public interface KitchenService {
    Future<ORDER_STATUS> processOrder(UUID orderId);
    void shutdown();
}
