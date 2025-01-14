package org.pancakelab.service;

import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.User;
import org.pancakelab.model.AuthorizationFailureException;
import org.pancakelab.model.PancakeServiceException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.pancakelab.util.PancakeUtils.authorizeUser;

public class AuthorizedDeliveryService implements DeliveryService {

    private final DeliveryService deliveryService;
    private final AuthenticationService authenticationService;

    public AuthorizedDeliveryService(
            final DeliveryService deliveryService,
            final AuthenticationService authenticationService
    ) {
        this.deliveryService = deliveryService;
        this.authenticationService = authenticationService;
    }

    private void authenticateUser(User user) throws PancakeServiceException {
        authenticationService.authenticate(user);
    }

    @Override
    public List<OrderDetails> viewCompletedOrders(User user) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, "delivery", 'R');
        return deliveryService.viewCompletedOrders(user);
    }

    @Override
    public void acceptOrder(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user,"delivery", 'C');
        deliveryService.acceptOrder(user, orderId);
    }

    @Override
    public void sendForTheDelivery(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user,"delivery",  'U');
        deliveryService.sendForTheDelivery(user, orderId);
    }
}
