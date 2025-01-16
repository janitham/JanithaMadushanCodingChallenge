package org.pancakelab.service;

import org.pancakelab.model.PancakeRecipe;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.User;

import java.util.Map;
import java.util.UUID;

public interface ChefService {
    Map<UUID, Map<PancakeRecipe,Integer>> viewOrders(User user) throws PancakeServiceException;
    void acceptOrder(User user, UUID orderId) throws PancakeServiceException;
    void notifyOrderCompletion(User user, UUID orderId) throws PancakeServiceException;
}
