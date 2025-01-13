package org.pancakelab.service;

import org.pancakelab.model.*;

import java.util.Map;
import java.util.UUID;

public interface OrderService {
    UUID createOrder(DeliveryInfo deliveryInformation, User user) throws PancakeServiceException;

    void addPancakes(UUID orderId, Map<PancakeMenu, Integer> pancakes, User user) throws PancakeServiceException;

    void complete(UUID orderId, User user) throws PancakeServiceException;

    void cancel(UUID orderId, User user) throws PancakeServiceException;

    Map<PancakeMenu, Integer> orderSummary(UUID orderId, User user) throws PancakeServiceException;

    OrderStatus status(UUID orderId, User user) throws PancakeServiceException;
}