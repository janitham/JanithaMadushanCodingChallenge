package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.pancakelab.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.pancakelab.util.PancakeUtils.USER_IS_NOT_AUTHORIZED;

public class AuthorizedDeliveryServiceTest {

    private DeliveryService deliveryService;
    private AuthenticationService authenticationService;
    private AuthorizedDeliveryService authorizedDeliveryService;
    private User privileged;
    private User unPrivileged;
    private User inCorrectPermissions;
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
        inCorrectPermissions = new User("testUser", "password".toCharArray(), new HashMap<>() {{
            put("kitchen", List.of('C', 'R', 'U', 'D'));
        }});
        orderId = UUID.randomUUID();
    }

    @Test
    public void shouldReturnCompletedOrdersWhenUserIsPrivileged() throws PancakeServiceException {
        // Given
        UUID orderId = UUID.randomUUID();
        DeliveryInfo deliveryInfo = mock(DeliveryInfo.class);
        Map<UUID, DeliveryInfo> orders = Map.of(orderId, deliveryInfo);
        when(deliveryService.viewCompletedOrders(privileged)).thenReturn(orders);
        // When
        Map<UUID, DeliveryInfo> result = authorizedDeliveryService.viewCompletedOrders(privileged);
        // Then
        assertEquals(orders, result);
        verify(authenticationService).authenticate(privileged);
        verify(deliveryService).viewCompletedOrders(privileged);
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

    @ParameterizedTest
    @ValueSource(strings = {"inCorrectPermissions", "unPrivileged"})
    public void whenViewingCompletedOrdersShouldThrowExceptionWhenUserIsNotPrivileged(String userType) {
        // Given
        User user = userType.equals("inCorrectPermissions") ? inCorrectPermissions : unPrivileged;
        // When
        // Then
        PancakeServiceException exception = assertThrows(
                AuthorizationFailureException.class, () -> authorizedDeliveryService.viewCompletedOrders(user));
        assertEquals(USER_IS_NOT_AUTHORIZED, exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"inCorrectPermissions", "unPrivileged"})
    public void whenAcceptingOrderShouldThrowExceptionWhenUserIsNotPrivileged(String userType) {
        // Given
        User user = userType.equals("inCorrectPermissions") ? inCorrectPermissions : unPrivileged;
        // When
        // Then
        PancakeServiceException exception = assertThrows(
                AuthorizationFailureException.class, () -> authorizedDeliveryService.acceptOrder(user, orderId));
        assertEquals(USER_IS_NOT_AUTHORIZED, exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"inCorrectPermissions", "unPrivileged"})
    public void whenSendingForDeliveryShouldThrowExceptionWhenUserIsNotPrivileged(String userType) {
        // Given
        User user = userType.equals("inCorrectPermissions") ? inCorrectPermissions : unPrivileged;
        // When
        // Then
        PancakeServiceException exception = assertThrows(
                AuthorizationFailureException.class, () -> authorizedDeliveryService.sendForTheDelivery(user, orderId));
        assertEquals(USER_IS_NOT_AUTHORIZED, exception.getMessage());
    }
}
