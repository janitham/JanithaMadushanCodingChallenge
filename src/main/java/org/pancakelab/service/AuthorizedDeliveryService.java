package org.pancakelab.service;

import org.pancakelab.model.DeliveryInfo;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.Privileges;
import org.pancakelab.model.User;

import java.util.Map;
import java.util.UUID;

import static org.pancakelab.util.PancakeUtils.authorizeUser;

/**
 * Service that provides authorized delivery operations.
 * This decorator class ensures that only authenticated and authorized users can access the delivery service.
 */
public class AuthorizedDeliveryService implements DeliveryService {

    private static final String DELIVERY_RESOURCE_NAME = "delivery";
    private final DeliveryService deliveryService;
    private final AuthenticationService authenticationService;

    /**
     * Constructs an AuthorizedDeliveryService with the specified delivery and authentication services.
     *
     * @param deliveryService the delivery service to delegate to
     * @param authenticationService the authentication service to use for user authentication
     */
    public AuthorizedDeliveryService(
            final DeliveryService deliveryService,
            final AuthenticationService authenticationService
    ) {
        this.deliveryService = deliveryService;
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
     * Returns a map of completed orders for the specified user.
     *
     * @param user the user whose completed orders are to be viewed
     * @return a map of completed orders
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public Map<UUID, DeliveryInfo> viewCompletedOrders(User user) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, DELIVERY_RESOURCE_NAME, Privileges.READ.getCode());
        return deliveryService.viewCompletedOrders(user);
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
        authorizeUser(user, DELIVERY_RESOURCE_NAME, Privileges.CREATE.getCode());
        deliveryService.acceptOrder(user, orderId);
    }

    /**
     * Sends the specified order for delivery for the specified user.
     *
     * @param user the user sending the order for delivery
     * @param orderId the ID of the order to send for delivery
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public void sendForTheDelivery(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, DELIVERY_RESOURCE_NAME, Privileges.UPDATE.getCode());
        deliveryService.sendForTheDelivery(user, orderId);
    }
}
