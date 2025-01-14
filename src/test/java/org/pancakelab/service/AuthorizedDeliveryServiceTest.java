package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthorizedDeliveryServiceTest {

    private DeliveryService deliveryService;
    private AuthenticationService authenticationService;
    private AuthorizedDeliveryService authorizedDeliveryService;
    private User privileged;
    private User unPrivileged;
    private UUID orderId;

    @BeforeEach
    public void setUp() {
        final Map<String, List<Character>> privileges = new HashMap<>() {
            {
                put("delivery", List.of('C', 'R', 'U', 'D'));
            }
        };
        deliveryService = mock(DeliveryService.class);
        authenticationService = mock(AuthenticationService.class);
        authorizedDeliveryService = new AuthorizedDeliveryService(deliveryService, authenticationService);
        privileged = new User("testUser", "password".toCharArray(), privileges);
        unPrivileged = new User("testUser", "password".toCharArray(), new HashMap<>());
        orderId = UUID.randomUUID();
    }

    @Test
    public void shouldReturnCompletedOrdersWhenUserIsPrivileged() throws PancakeServiceException {
        // Given
        List<OrderDetails> orders = List.of(mock(OrderDetails.class));
        when(deliveryService.viewCompletedOrders(privileged)).thenReturn(orders);
        // When
        List<OrderDetails> result = authorizedDeliveryService.viewCompletedOrders(privileged);
        // Then
        assertEquals(orders, result);
        verify(authenticationService).authenticate(privileged);
    }

    @Test
    public void shouldAcceptOrderWhenUserIsPrivileged() throws PancakeServiceException {
        // Given
        doNothing().when(deliveryService).acceptOrder(privileged, orderId);
        // When
        authorizedDeliveryService.acceptOrder(privileged, orderId);
        // Then
        verify(authenticationService).authenticate(privileged);
        verify(deliveryService).acceptOrder(privileged, orderId);
    }

    @Test
    public void shouldSendForDeliveryWhenUserIsPrivileged() throws PancakeServiceException {
        // Given
        doNothing().when(deliveryService).sendForTheDelivery(privileged, orderId);
        // When
        authorizedDeliveryService.sendForTheDelivery(privileged, orderId);
        // Then
        verify(authenticationService).authenticate(privileged);
        verify(deliveryService).sendForTheDelivery(privileged, orderId);
    }
}
