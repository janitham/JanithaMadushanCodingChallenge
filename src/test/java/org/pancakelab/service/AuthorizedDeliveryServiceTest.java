package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthorizedDeliveryServiceTest {

    private DeliveryService deliveryService;
    private AuthenticationService authenticationService;
    private ConcurrentHashMap<UUID, User> orderUserMap;
    private AuthorizedDeliveryService authorizedDeliveryService;
    private User user;
    private UUID orderId;

    @BeforeEach
    public void setUp() {
        deliveryService = mock(DeliveryService.class);
        authenticationService = mock(AuthenticationService.class);
        orderUserMap = new ConcurrentHashMap<>();
        authorizedDeliveryService = new AuthorizedDeliveryService(deliveryService, authenticationService);
        user = new User("testUser", "password".toCharArray());
        orderId = UUID.randomUUID();
        orderUserMap.put(orderId, user);
    }

    @Test
    public void testViewCompletedOrders() throws PancakeServiceException {
        List<OrderDetails> orders = List.of(mock(OrderDetails.class));
        when(deliveryService.viewCompletedOrders(user)).thenReturn(orders);

        List<OrderDetails> result = authorizedDeliveryService.viewCompletedOrders(user);

        assertEquals(orders, result);
        verify(authenticationService).authenticate(user);
    }

    @Test
    public void testAcceptOrder() throws PancakeServiceException {
        doNothing().when(deliveryService).acceptOrder(user, orderId);

        authorizedDeliveryService.acceptOrder(user, orderId);

        verify(authenticationService).authenticate(user);
        verify(deliveryService).acceptOrder(user, orderId);
    }

    @Test
    public void testSendForTheDelivery() throws PancakeServiceException {
        doNothing().when(deliveryService).sendForTheDelivery(user, orderId);

        authorizedDeliveryService.sendForTheDelivery(user, orderId);

        verify(authenticationService).authenticate(user);
        verify(deliveryService).sendForTheDelivery(user, orderId);
    }

    //@Test
    public void testUnauthorizedAccess() {
        User anotherUser = new User("anotherUser", "password".toCharArray());
        UUID anotherOrderId = UUID.randomUUID();
        orderUserMap.put(anotherOrderId, anotherUser);

        AuthorizationFailureException exception = assertThrows(
                AuthorizationFailureException.class,
                () -> authorizedDeliveryService.acceptOrder(user, anotherOrderId)
        );

        assertEquals("User not authorized to access order", exception.getMessage());
    }
}
