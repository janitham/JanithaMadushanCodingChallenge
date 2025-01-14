package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.AuthenticationFailureException;
import org.pancakelab.model.OrderDetails;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.pancakelab.service.AuthenticationServiceImpl.USER_IS_NOT_AUTHENTICATED;
import static org.pancakelab.util.PancakeUtils.USER_IS_NOT_AUTHORIZED;

public class AuthorizedKitchenServiceTest {
    private KitchenService kitchenService;
    private AuthenticationService authenticationService;
    private AuthorizedKitchenService authorizedKitchenService;
    private User privileged;
    private User unPrivileged;
    private UUID orderId;

    @BeforeEach
    public void setUp() {
        final Map<String, List<Character>> privileges = new HashMap<>() {
            {
                put("order", List.of('C', 'R', 'U', 'D'));
                put("kitchen", List.of('C', 'R', 'U', 'D'));
                put("delivery", List.of('C', 'R', 'U', 'D'));
            }
        };
        kitchenService = mock(KitchenService.class);
        authenticationService = mock(AuthenticationService.class);
        authorizedKitchenService = new AuthorizedKitchenService(kitchenService, authenticationService);
        privileged = new User("testUser", "password".toCharArray(), privileges);
        unPrivileged = new User("testUser", "password".toCharArray(), new HashMap<>());
        orderId = UUID.randomUUID();
    }

    @Test
    public void givenAuthenticatedUser_whenViewOrders_thenReturnsOrders() throws PancakeServiceException {
        // Given
        List<OrderDetails> orders = List.of(mock(OrderDetails.class));
        when(kitchenService.viewOrders(privileged)).thenReturn(orders);
        // When
        List<OrderDetails> result = authorizedKitchenService.viewOrders(privileged);
        // Then
        verify(authenticationService).authenticate(privileged);
        verify(kitchenService).viewOrders(privileged);
        assertEquals(orders, result);
    }

    @Test
    public void givenAuthenticatedUser_whenAcceptOrder_thenOrderIsAccepted() throws PancakeServiceException {
        // Given
        doNothing().when(kitchenService).acceptOrder(privileged, orderId);
        // When
        authorizedKitchenService.acceptOrder(privileged, orderId);
        // Then
        verify(authenticationService).authenticate(privileged);
        verify(kitchenService).acceptOrder(privileged, orderId);
    }

    @Test
    public void givenAuthenticatedUser_whenNotifyOrderCompletion_thenOrderIsCompleted() throws PancakeServiceException {
        // Given
        doNothing().when(kitchenService).notifyOrderCompletion(privileged, orderId);
        // When
        authorizedKitchenService.notifyOrderCompletion(privileged, orderId);
        // Then
        verify(authenticationService).authenticate(privileged);
        verify(kitchenService).notifyOrderCompletion(privileged, orderId);
    }

    @Test
    public void givenUnauthenticatedUser_whenViewOrders_thenThrowsException() throws AuthenticationFailureException {
        // Given
        doThrow(new AuthenticationFailureException(USER_IS_NOT_AUTHENTICATED)).when(authenticationService).authenticate(privileged);
        // When
        // Then
        PancakeServiceException exception = assertThrows(
                PancakeServiceException.class,
                () -> authorizedKitchenService.viewOrders(privileged)
        );
        assertEquals(USER_IS_NOT_AUTHENTICATED, exception.getMessage());
    }

    @Test
    public void givenUnprivilegedUser_whenViewOrders_thenThrowsException() {
        // Given
        // When
        // Then
        PancakeServiceException exception = assertThrows(
                PancakeServiceException.class,
                () -> authorizedKitchenService.viewOrders(unPrivileged)
        );
        assertEquals(USER_IS_NOT_AUTHORIZED, exception.getMessage());
    }

    @Test
    public void givenUnprivilegedUser_whenAcceptOrder_thenThrowsException() {
        // Given
        // When
        // Then
        PancakeServiceException exception = assertThrows(
                PancakeServiceException.class,
                () -> authorizedKitchenService.acceptOrder(unPrivileged, orderId)
        );
        assertEquals(USER_IS_NOT_AUTHORIZED, exception.getMessage());
    }

    @Test
    public void givenUnprivilegedUser_whenNotifyOrderCompletion_thenThrowsException() {
        // Given
        // When
        // Then
        PancakeServiceException exception = assertThrows(
                PancakeServiceException.class,
                () -> authorizedKitchenService.notifyOrderCompletion(unPrivileged, orderId)
        );
        assertEquals(USER_IS_NOT_AUTHORIZED, exception.getMessage());
    }
}
