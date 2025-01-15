package org.pancakelab.service;

import org.pancakelab.model.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.pancakelab.util.PancakeUtils.authorizeUser;

public class AuthorizedOrderService implements OrderService {

    public static final String SERVICE_NAME = "order";
    public static final String USER_DOES_NOT_HAVE_AUTHORITY_TO_ACCESS_ORDER = "User not authorized to access order";
    public static final String ORDER_NOT_FOUND = "Order not found";

    private final OrderService orderService;
    private final AuthenticationService authenticationService;
    private final ConcurrentHashMap<UUID, User> orderUserMap = new ConcurrentHashMap<>();

    public AuthorizedOrderService(
            final OrderService orderService,
            final AuthenticationService authenticationService
    ) {
        this.orderService = orderService;
        this.authenticationService = authenticationService;
    }

    private void authorizeOrderAccess(User user, UUID orderId) throws AuthorizationFailureException {
        if (orderUserMap.get(orderId) == null) {
            throw new AuthorizationFailureException(ORDER_NOT_FOUND);
        }
        if (!orderUserMap.get(orderId).equals(user)) {
            throw new AuthorizationFailureException(USER_DOES_NOT_HAVE_AUTHORITY_TO_ACCESS_ORDER);
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
    public UUID createOrder(User user, DeliveryInfo deliveryInformation) throws PancakeServiceException {
        authenticateUser(user);
        authorizeUser(user, SERVICE_NAME, Privileges.CREATE.getCode());
        var orderId = orderService.createOrder(user, deliveryInformation);
        assignOrderToUser(orderId, user);
        return orderId;
    }

    @Override
    public void addPancakes(User user, UUID orderId, Map<Pancakes, Integer> pancakes) throws PancakeServiceException {
        authenticateUser(user);
        authorizeOrderAccess(user, orderId);
        authorizeUser(user, SERVICE_NAME, Privileges.CREATE.getCode());
        orderService.addPancakes(user, orderId, pancakes);
    }

    @Override
    public Map<Pancakes, Integer> orderSummary(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeOrderAccess(user, orderId);
        authorizeUser(user, SERVICE_NAME, Privileges.READ.getCode());
        return orderService.orderSummary(user, orderId);
    }

    @Override
    public OrderStatus status(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeOrderAccess(user, orderId);
        authorizeUser(user, SERVICE_NAME, Privileges.READ.getCode());
        return orderService.status(user, orderId);
    }

    @Override
    public void complete(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeOrderAccess(user, orderId);
        authorizeUser(user, SERVICE_NAME, Privileges.CREATE.getCode());
        orderService.complete(user, orderId);
        unAssignOrderFromUser(orderId);
    }

    @Override
    public void cancel(User user, UUID orderId) throws PancakeServiceException {
        authenticateUser(user);
        authorizeOrderAccess(user, orderId);
        authorizeUser(user, SERVICE_NAME, Privileges.UPDATE.getCode());
        orderService.cancel(user, orderId);
        unAssignOrderFromUser(orderId);
    }
}