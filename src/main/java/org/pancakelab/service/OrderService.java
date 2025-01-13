package org.pancakelab.service;

import org.pancakelab.model.DeliveryInfo;
import org.pancakelab.model.ORDER_STATUS;
import org.pancakelab.model.PancakeMenu;
import org.pancakelab.model.PancakeServiceException;

import java.util.Map;
import java.util.UUID;

public interface OrderService {
    UUID createOrder(DeliveryInfo deliveryInformation) throws PancakeServiceException;

    void addPancakes(UUID orderId, Map<PancakeMenu, Integer> pancakes);

    void complete(UUID orderId);

    void cancel(UUID orderId);

    Map<PancakeMenu, Integer> orderSummary(UUID orderId);

    ORDER_STATUS status(UUID orderId);
}