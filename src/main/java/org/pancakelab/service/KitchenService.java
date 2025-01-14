package org.pancakelab.service;

import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.User;

import java.util.List;
import java.util.UUID;

public interface KitchenService {
    List<OrderDetails> viewOrders(User user) throws PancakeServiceException;

    void acceptOrder(User user, UUID orderId) throws PancakeServiceException;

    void notifyOrderCompletion(User user, UUID orderId) throws PancakeServiceException;
}
