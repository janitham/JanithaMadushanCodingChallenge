package org.pancakelab.service;

import org.pancakelab.model.DeliveryInfo;
import org.pancakelab.model.ORDER_STATUS;
import org.pancakelab.model.PancakeMenu;
import org.pancakelab.model.PancakeServiceException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

public interface OrderService {
    UUID createOrder(DeliveryInfo deliveryInformation) throws PancakeServiceException;

    void addPancakes(UUID orderId, Map<PancakeMenu, Integer> pancakes);

    Future<ORDER_STATUS> complete(UUID orderId);

    void cancel(UUID orderId);

    Map<PancakeMenu, Integer> orderSummary(UUID orderId);
}