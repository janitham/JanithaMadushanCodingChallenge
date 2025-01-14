package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.User;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class AuthorizedKitchenServiceTest {
    private KitchenService kitchenService;
    private AuthenticationService authenticationService;
    private AuthorizedKitchenService authorizedKitchenService;
    private User user;
    private UUID orderId;

    @BeforeEach
    public void setUp() {
        kitchenService = mock(KitchenService.class);
        authenticationService = mock(AuthenticationService.class);
        authorizedKitchenService = new AuthorizedKitchenService(kitchenService, authenticationService);
        user = new User("testUser", "password".toCharArray());
        orderId = UUID.randomUUID();
    }

    @Test
    public void givenAuthenticatedUser_whenViewOrders_thenReturnsOrders() throws PancakeServiceException {
        List<OrderDetails> orders = List.of(mock(OrderDetails.class));
        when(kitchenService.viewOrders(user)).thenReturn(orders);

        List<OrderDetails> result = authorizedKitchenService.viewOrders(user);

        verify(authenticationService).authenticate(user);
        verify(kitchenService).viewOrders(user);
        assertEquals(orders, result);
    }

    @Test
    public void givenAuthenticatedUser_whenAcceptOrder_thenOrderIsAccepted() throws PancakeServiceException {
        doNothing().when(kitchenService).acceptOrder(user, orderId);

        authorizedKitchenService.acceptOrder(user, orderId);

        verify(authenticationService).authenticate(user);
        verify(kitchenService).acceptOrder(user, orderId);
    }

    @Test
    public void givenAuthenticatedUser_whenNotifyOrderCompletion_thenOrderIsCompleted() throws PancakeServiceException {
        doNothing().when(kitchenService).notifyOrderCompletion(user, orderId);

        authorizedKitchenService.notifyOrderCompletion(user, orderId);

        verify(authenticationService).authenticate(user);
        verify(kitchenService).notifyOrderCompletion(user, orderId);
    }

    //@Test
    public void givenAuthenticationFails_whenViewOrders_thenThrowsException() throws PancakeServiceException {
        doThrow(new PancakeServiceException("Authentication failed")).when(authenticationService).authenticate(user);

        assertThrows(PancakeServiceException.class, () -> authorizedKitchenService.viewOrders(user));

        verify(authenticationService).authenticate(user);
        verify(kitchenService, never()).viewOrders(user);
    }
}
