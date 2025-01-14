package org.pancakelab.service;

import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.User;
import org.pancakelab.model.AuthorizationFailureException;
import org.pancakelab.model.PancakeServiceException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthorizedDeliveryService implements DeliveryService {
    private final DeliveryService deliveryService;
    private final AuthenticationService authenticationService;
    //private final ConcurrentHashMap<UUID, User> orderUserMap;

    public AuthorizedDeliveryService(
            final DeliveryService deliveryService,
            final AuthenticationService authenticationService
            //final ConcurrentHashMap<UUID, User> orderUserMap
    ) {
        this.deliveryService = deliveryService;
        this.authenticationService = authenticationService;
        //this.orderUserMap = orderUserMap;
    }

    /*private void authorizeOrderAccess(User user, UUID orderId) throws AuthorizationFailureException {
        if (orderUserMap.get(orderId) == null) {
            throw new AuthorizationFailureException("Order not found");
        }
        if (!orderUserMap.get(orderId).equals(user)) {
            throw new AuthorizationFailureException("User not authorized to access order");
        }
    }*/

    private void authenticateUser(User user) throws PancakeServiceException {
        authenticationService.authenticate(user);
    }

    @Override
    public List<OrderDetails> viewCompletedOrders(User user) throws PancakeServiceException {
        authenticateUser(user);
        return deliveryService.viewCompletedOrders(user);
    }

    @Override
    public void acceptOrder(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        //authorizeOrderAccess(user, orderId);
        deliveryService.acceptOrder(user, orderId);
    }

    @Override
    public void sendForTheDelivery(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        //authorizeOrderAccess(user, orderId);
        deliveryService.sendForTheDelivery(user, orderId);
    }
}
