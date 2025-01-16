package org.pancakelab.service;

import org.pancakelab.model.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.pancakelab.util.PancakeUtils.authorizeUser;

/**
 * Service that provides authorized order operations.
 * This decorator class ensures that only authenticated and authorized users can access the order service.
 */
public class AuthorizedOrderService implements OrderService {

    public static final String SERVICE_NAME = "order";
    public static final String USER_DOES_NOT_HAVE_AUTHORITY_TO_ACCESS_ORDER = "User not authorized to access order";
    public static final String ORDER_NOT_FOUND = "Order not found";

    private final OrderService orderService;
    private final AuthenticationService authenticationService;
    private final ConcurrentHashMap<UUID, User> orderUserMap = new ConcurrentHashMap<>();

    /**
     * Constructs an AuthorizedOrderService with the specified order and authentication services.
     *
     * @param orderService          the order service to delegate to
     * @param authenticationService the authentication service to use for user authentication
     */
    public AuthorizedOrderService(
            final OrderService orderService,
            final AuthenticationService authenticationService
    ) {
        this.orderService = orderService;
        this.authenticationService = authenticationService;
    }

    /**
     * Authorizes access to the specified order for the specified user.
     *
     * @param user    the user to authorize
     * @param orderId the ID of the order to authorize access to
     * @throws AuthorizationFailureException if the user is not authorized to access the order
     */
    private void authorizeOrderAccess(User user, UUID orderId) throws AuthorizationFailureException {
        if (orderUserMap.get(orderId) == null) {
            throw new AuthorizationFailureException(ORDER_NOT_FOUND);
        }
        if (!orderUserMap.get(orderId).equals(user)) {
            throw new AuthorizationFailureException(USER_DOES_NOT_HAVE_AUTHORITY_TO_ACCESS_ORDER);
        }
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
     * Assigns the specified order to the specified user.
     *
     * @param orderId the ID of the order to assign
     * @param user    the user to assign the order to
     */
    private void assignOrderToUser(UUID orderId, User user) {
        orderUserMap.put(orderId, user);
    }

    /**
     * Unassigns the specified order from any user.
     *
     * @param orderId the ID of the order to unassign
     */
    private void unAssignOrderFromUser(UUID orderId) {
        orderUserMap.remove(orderId);
    }

    /**
     * Creates a new order for the specified user with the specified delivery information.
     *
     * @param user                the user creating the order
     * @param deliveryInformation the delivery information for the order
     * @return the ID of the created order
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public UUID createOrder(User user, DeliveryInfo deliveryInformation) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, SERVICE_NAME, Privileges.CREATE.getCode());
        var orderId = orderService.createOrder(user, deliveryInformation);
        assignOrderToUser(orderId, user);
        return orderId;
    }

    /**
     * Adds pancakes to the specified order for the specified user.
     *
     * @param user     the user adding pancakes to the order
     * @param orderId  the ID of the order to add pancakes to
     * @param pancakes the pancakes to add to the order
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public void addPancakes(User user, UUID orderId, Map<PancakeRecipe, Integer> pancakes) throws PancakeServiceException {
        authenticateUser(user);
        authorizeOrderAccess(user, orderId);
        authorizeUser(user, SERVICE_NAME, Privileges.CREATE.getCode());
        orderService.addPancakes(user, orderId, pancakes);
    }

    /**
     * Returns a summary of the specified order for the specified user.
     *
     * @param user    the user whose order summary is to be viewed
     * @param orderId the ID of the order to view the summary of
     * @return a map of pancakes and their quantities in the order
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public Map<PancakeRecipe, Integer> orderSummary(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeOrderAccess(user, orderId);
        authorizeUser(user, SERVICE_NAME, Privileges.READ.getCode());
        return orderService.orderSummary(user, orderId);
    }

    /**
     * Returns the status of the specified order for the specified user.
     *
     * @param user    the user whose order status is to be viewed
     * @param orderId the ID of the order to view the status of
     * @return the status of the order
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public OrderStatus status(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeOrderAccess(user, orderId);
        authorizeUser(user, SERVICE_NAME, Privileges.READ.getCode());
        return orderService.status(user, orderId);
    }

    /**
     * Completes the specified order for the specified user.
     *
     * @param user    the user completing the order
     * @param orderId the ID of the order to complete
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public void complete(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeOrderAccess(user, orderId);
        authorizeUser(user, SERVICE_NAME, Privileges.CREATE.getCode());
        orderService.complete(user, orderId);
        unAssignOrderFromUser(orderId);
    }

    /**
     * Cancels the specified order for the specified user.
     *
     * @param user    the user canceling the order
     * @param orderId the ID of the order to cancel
     * @throws PancakeServiceException if the user cannot be authenticated or authorized
     */
    @Override
    public void cancel(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeOrderAccess(user, orderId);
        authorizeUser(user, SERVICE_NAME, Privileges.UPDATE.getCode());
        orderService.cancel(user, orderId);
        unAssignOrderFromUser(orderId);
    }
}