package org.pancakelab.service;

import org.pancakelab.model.ORDER_STATUS;
import org.pancakelab.model.OrderDetails;

import java.util.UUID;
import java.util.concurrent.Future;

public interface OrderService {

    UUID open(OrderDetails orderDetails);

    void cancel(UUID orderId);

    Future<ORDER_STATUS> complete(UUID orderId);

}