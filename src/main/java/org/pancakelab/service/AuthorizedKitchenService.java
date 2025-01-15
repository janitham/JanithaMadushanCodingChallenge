package org.pancakelab.service;

import org.pancakelab.model.PancakeRecipe;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.Privileges;
import org.pancakelab.model.User;

import java.util.Map;
import java.util.UUID;

import static org.pancakelab.util.PancakeUtils.authorizeUser;

public class AuthorizedKitchenService implements KitchenService {
    public static final String SERVICE_NAME = "kitchen";
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
    public Map<UUID, Map<PancakeRecipe, Integer>> viewOrders(User user) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, SERVICE_NAME, Privileges.READ.getCode());
        return kitchenService.viewOrders(user);
    }

    @Override
    public void acceptOrder(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, SERVICE_NAME, Privileges.CREATE.getCode());
        kitchenService.acceptOrder(user, orderId);
    }

    @Override
    public void notifyOrderCompletion(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, SERVICE_NAME, Privileges.UPDATE.getCode());
        kitchenService.notifyOrderCompletion(user, orderId);
    }
}
