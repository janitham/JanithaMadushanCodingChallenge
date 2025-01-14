package org.pancakelab.service;

import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.User;

import java.util.List;
import java.util.UUID;

public class AuthorizedKitchenService implements KitchenService {
    private final KitchenService kitchenService;
    private final AuthenticationService authenticationService;

    public AuthorizedKitchenService(
            final KitchenService kitchenService,
            final AuthenticationService authenticationService
    ) {
        this.kitchenService = kitchenService;
        this.authenticationService = authenticationService;
    }

    private void authenticateUser(User user) throws PancakeServiceException {
        authenticationService.authenticate(user);
    }

    @Override
    public List<OrderDetails> viewOrders(User user) throws PancakeServiceException {
        authenticateUser(user);
        return kitchenService.viewOrders(user);
    }

    @Override
    public void acceptOrder(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        kitchenService.acceptOrder(user, orderId);
    }

    @Override
    public void notifyOrderCompletion(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        kitchenService.notifyOrderCompletion(user, orderId);
    }
}
