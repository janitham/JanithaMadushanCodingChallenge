package org.pancakelab.service;

import org.pancakelab.model.*;

import java.util.Map;
import java.util.UUID;

public class AuthenticatedOrderService implements OrderService {

    private final OrderService orderService;
    private final AuthenticationService authenticationService;

    public AuthenticatedOrderService(
            final OrderService orderService,
            final AuthenticationService authenticationService
    ) {
        this.orderService = orderService;
        this.authenticationService = authenticationService;
    }

    @Override
    public UUID createOrder(DeliveryInfo deliveryInformation, User user) throws PancakeServiceException {
        authenticationService.authenticate(user);
        return orderService.createOrder(deliveryInformation, user);
    }

    @Override
    public void addPancakes(UUID orderId, Map<PancakeMenu, Integer> pancakes, User user) throws PancakeServiceException {
        authenticationService.authenticate(user);
        orderService.addPancakes(orderId, pancakes, user);
    }

    @Override
    public Map<PancakeMenu, Integer> orderSummary(UUID orderId, User user) throws PancakeServiceException {
        authenticationService.authenticate(user);
        return orderService.orderSummary(orderId, user);
    }

    @Override
    public OrderStatus status(UUID orderId, User user) throws PancakeServiceException {
        authenticationService.authenticate(user);
        return orderService.status(orderId, user);
    }

    @Override
    public void complete(UUID orderId, User user) throws PancakeServiceException {
        authenticationService.authenticate(user);
        orderService.complete(orderId, user);
    }

    @Override
    public void cancel(UUID orderId, User user) throws PancakeServiceException {
        authenticationService.authenticate(user);
        orderService.cancel(orderId, user);
    }
}