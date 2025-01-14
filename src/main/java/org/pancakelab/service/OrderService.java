package org.pancakelab.service;

import org.pancakelab.model.*;

import java.util.Map;
import java.util.UUID;

public interface OrderService {
    UUID createOrder(User user, DeliveryInfo deliveryInformation) throws PancakeServiceException;

    void addPancakes(User user, UUID orderId, Map<Pancakes, Integer> pancakes) throws PancakeServiceException;

    void complete(User user, UUID orderId) throws PancakeServiceException;

    void cancel(User user, UUID orderId) throws PancakeServiceException;

    Map<Pancakes, Integer> orderSummary(User user, UUID orderId) throws PancakeServiceException;

    OrderStatus status(User user, UUID orderId) throws PancakeServiceException;
}