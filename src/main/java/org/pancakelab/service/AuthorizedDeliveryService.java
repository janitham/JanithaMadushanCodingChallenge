package org.pancakelab.service;

import org.pancakelab.model.DeliveryInfo;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.Privileges;
import org.pancakelab.model.User;

import java.util.Map;
import java.util.UUID;

import static org.pancakelab.util.PancakeUtils.authorizeUser;

public class AuthorizedDeliveryService implements DeliveryService {

    private static final String SERVICE_NAME = "delivery";
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
    public Map<UUID, DeliveryInfo> viewCompletedOrders(User user) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, SERVICE_NAME, Privileges.READ.getCode());
        return deliveryService.viewCompletedOrders(user);
    }

    @Override
    public void acceptOrder(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user,SERVICE_NAME, Privileges.CREATE.getCode());
        deliveryService.acceptOrder(user, orderId);
    }

    @Override
    public void sendForTheDelivery(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user,SERVICE_NAME,  Privileges.UPDATE.getCode());
        deliveryService.sendForTheDelivery(user, orderId);
    }
}
