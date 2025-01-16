package org.pancakelab.service;

import org.pancakelab.model.PancakeRecipe;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.Privileges;
import org.pancakelab.model.User;

import java.util.Map;
import java.util.UUID;

import static org.pancakelab.util.PancakeUtils.authorizeUser;

/**
 * Service that provides authorized kitchen operations.
 * This decorator class ensures that only authenticated and authorized users can access the kitchen service.
 */
public class AuthorizedKitchenService implements ChefService {
    public static final String SERVICE_NAME = "kitchen";
    private final ChefService chefService;
    private final AuthenticationService authenticationService;

    /**
     * Constructs an AuthorizedKitchenService with the specified kitchen and authentication services.
     *
     * @param chefService the kitchen service to delegate to
     * @param authenticationService the authentication service to use for user authentication
     */
    public AuthorizedKitchenService(
            final ChefService chefService,
            final AuthenticationService authenticationService
    ) {
        this.chefService = chefService;
        this.authenticationService = authenticationService;
    }

    /**
     * Authenticates the specified user.
     *
     * @param user the user to authenticate
     * @throws PancakeServiceException if the user cannot be authenticated
     */
    private void authenticateUser(User user) throws PancakeServiceException {
        authenticationService.authenticate(user);
    }

    /**
     * Returns a map of orders for the specified user.
     *
     * @param user the user whose orders are to be viewed
     * @return a map of orders
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public Map<UUID, Map<PancakeRecipe, Integer>> viewOrders(User user) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, SERVICE_NAME, Privileges.READ.getCode());
        return chefService.viewOrders(user);
    }

    /**
     * Accepts the specified order for the specified user.
     *
     * @param user the user accepting the order
     * @param orderId the ID of the order to accept
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public void acceptOrder(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, SERVICE_NAME, Privileges.CREATE.getCode());
        chefService.acceptOrder(user, orderId);
    }

    /**
     * Notifies the completion of the specified order for the specified user.
     *
     * @param user the user notifying the order completion
     * @param orderId the ID of the order to notify completion
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public void notifyOrderCompletion(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, SERVICE_NAME, Privileges.UPDATE.getCode());
        chefService.notifyOrderCompletion(user, orderId);
    }
}
