package org.pancakelab.service;

import org.pancakelab.model.DeliveryInfo;
import org.pancakelab.model.ORDER_STATUS;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.util.PancakeFactoryMenu;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

public interface OrderService {

    void cancel(UUID orderId);

    Future<ORDER_STATUS> complete(UUID orderId);

    UUID createOrder(DeliveryInfo deliveryInformation) throws PancakeServiceException;

    void addPancakes(UUID orderId, Map<PancakeFactoryMenu.PANCAKE_TYPE, Integer> pancakes);

    Map<PancakeFactoryMenu.PANCAKE_TYPE,Integer> orderSummary(UUID orderId);
}