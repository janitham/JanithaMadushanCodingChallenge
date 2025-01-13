package org.pancakelab.service;

import org.pancakelab.model.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthenticatedOrderService implements OrderService {

    private final OrderService orderService;
    private final AuthenticationService authenticationService;
    private final ConcurrentHashMap<UUID, User> orderUserMap = new ConcurrentHashMap<>();

    public AuthenticatedOrderService(
            final OrderService orderService,
            final AuthenticationService authenticationService
    ) {
        this.orderService = orderService;
        this.authenticationService = authenticationService;
    }

    private void authorizeOrderAccess(UUID orderId, User user) throws AuthenticationFailureException {
        if (orderUserMap.get(orderId) == null) {
            throw new AuthenticationFailureException("Order not found");
        }
        if (!orderUserMap.get(orderId).equals(user)) {
            throw new AuthenticationFailureException("User not authorized to access order");
        }
    }

    private void authenticateUser(User user) throws PancakeServiceException {
        authenticationService.authenticate(user);
    }

    private void assignOrderToUser(UUID orderId, User user) {
        orderUserMap.put(orderId, user);
    }

    private void unAssignOrderFromUser(UUID orderId) {
        orderUserMap.remove(orderId);
    }

    @Override
    public UUID createOrder(DeliveryInfo deliveryInformation, User user) throws PancakeServiceException {
        authenticateUser(user);
        var orderId = orderService.createOrder(deliveryInformation, user);
        // null pointer exception here
        assignOrderToUser(orderId, user);
        return orderId;
    }

    @Override
    public void addPancakes(UUID orderId, Map<PancakeMenu, Integer> pancakes, User user) throws PancakeServiceException {
        authenticateUser(user);
        authorizeOrderAccess(orderId, user);
        orderService.addPancakes(orderId, pancakes, user);
    }

    @Override
    public Map<PancakeMenu, Integer> orderSummary(UUID orderId, User user) throws PancakeServiceException {
        authenticateUser(user);
        authorizeOrderAccess(orderId, user);
        return orderService.orderSummary(orderId, user);
    }

    @Override
    public OrderStatus status(UUID orderId, User user) throws PancakeServiceException {
        authenticateUser(user);
        authorizeOrderAccess(orderId, user);
        return orderService.status(orderId, user);
    }

    @Override
    public void complete(UUID orderId, User user) throws PancakeServiceException {
        authenticateUser(user);
        authorizeOrderAccess(orderId, user);
        orderService.complete(orderId, user);
        unAssignOrderFromUser(orderId);
    }

    @Override
    public void cancel(UUID orderId, User user) throws PancakeServiceException {
        authenticateUser(user);
        authorizeOrderAccess(orderId, user);
        orderService.cancel(orderId, user);
        unAssignOrderFromUser(orderId);
    }
}