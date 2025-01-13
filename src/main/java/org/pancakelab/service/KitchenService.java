package org.pancakelab.service;

import java.util.UUID;

public interface KitchenService {
    void processOrder(UUID orderId);
    void shutdown();
}
