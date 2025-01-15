package org.pancakelab.service;

import org.pancakelab.model.DeliveryInfo;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.User;

import java.util.Map;
import java.util.UUID;

public interface DeliveryService {
    Map<UUID, DeliveryInfo> viewCompletedOrders(User user) throws PancakeServiceException;
    void acceptOrder(User user, UUID orderId) throws PancakeServiceException;
    void sendForTheDelivery(User user, UUID orderId) throws PancakeServiceException;
}
