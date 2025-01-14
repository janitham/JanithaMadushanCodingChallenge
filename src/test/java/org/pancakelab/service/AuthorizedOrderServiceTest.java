package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pancakelab.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.pancakelab.service.AuthenticationServiceImpl.USER_IS_NOT_AUTHENTICATED;

public class AuthorizedOrderServiceTest {

    private OrderService orderService;
    private AuthenticationService authenticationService;
    private AuthorizedOrderService authorizedOrderService;
    private User testUser;
    private DeliveryInfo deliveryInfo;
    private final UUID testOrderId = UUID.randomUUID();
    private final Map<Pancakes, Integer> testPancakes = new HashMap<>() {
        {
            put(Pancakes.DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE, 1);
            put(Pancakes.MILK_CHOCOLATE_PANCAKE, 2);
        }
    };

    @BeforeEach
    public void setUp() {
        testUser = new User("testUser", "password".toCharArray());
        authenticationService = Mockito.mock(AuthenticationService.class);
        orderService = Mockito.mock(OrderService.class);
        authorizedOrderService = new AuthorizedOrderService(orderService, authenticationService);
        deliveryInfo = new DeliveryInfo("1", "2");
    }

    @Test
    public void shouldAuthenticateUserWhenCreatingOrder() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        // When
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        // Then
        verify(authenticationService).authenticate(testUser);
        verify(orderService).createOrder(testUser, deliveryInfo);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForCreateOrder() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException(USER_IS_NOT_AUTHENTICATED)).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authorizedOrderService.createOrder(testUser, deliveryInfo));
    }

    @Test
    public void shouldAuthenticateUserWhenAddingPancakes() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        // When
        authorizedOrderService.addPancakes(testUser, testOrderId, testPancakes);
        // Then
        verify(authenticationService, times(2)).authenticate(testUser);
        verify(orderService).addPancakes(testUser, testOrderId, testPancakes);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForAddPancakes() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException(USER_IS_NOT_AUTHENTICATED)).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authorizedOrderService.addPancakes(testUser, testOrderId, testPancakes));
    }

    @Test
    public void shouldAuthenticateUserWhenGettingOrderSummary() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        // When
        when(orderService.orderSummary(testUser, testOrderId)).thenReturn(testPancakes);
        var summary = authorizedOrderService.orderSummary(testUser, testOrderId);
        // Then
        verify(authenticationService, times(2)).authenticate(testUser);
        assertNotNull(summary);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForOrderSummary() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException(USER_IS_NOT_AUTHENTICATED)).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authorizedOrderService.orderSummary(testUser, testOrderId));
    }

    @Test
    public void shouldAuthenticateUserWhenGettingOrderStatus() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        // When
        authorizedOrderService.status(testUser, testOrderId);
        // Then
        verify(authenticationService, times(2)).authenticate(testUser);
        verify(orderService).status(testUser, testOrderId);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForOrderStatus() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException(USER_IS_NOT_AUTHENTICATED)).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authorizedOrderService.status(testUser, testOrderId));
    }

    @Test
    public void shouldAuthenticateUserWhenCompletingOrder() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        // When
        authorizedOrderService.complete(testUser, testOrderId);
        // Then
        verify(authenticationService, times(2)).authenticate(testUser);
        verify(orderService).complete(testUser, testOrderId);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForCompleteOrder() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException(USER_IS_NOT_AUTHENTICATED)).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authorizedOrderService.complete(testUser, testOrderId));
    }

    @Test
    public void shouldAuthenticateUserWhenCancellingOrder() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        // When
        authorizedOrderService.cancel(testUser, testOrderId);
        // Then
        verify(authenticationService, times(2)).authenticate(testUser);
        verify(orderService).cancel(testUser, testOrderId);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForCancelOrder() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException(USER_IS_NOT_AUTHENTICATED)).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authorizedOrderService.cancel(testUser, testOrderId));
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthorizedForAddPancakes() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        User unauthorizedUser = new User("unauthorizedUser", "password".toCharArray());
        // When
        // Then
        assertThrows(AuthorizationFailureException.class,
                () -> authorizedOrderService.addPancakes(unauthorizedUser, testOrderId, testPancakes));
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthorizedForOrderSummary() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        User unauthorizedUser = new User("unauthorizedUser", "password".toCharArray());
        // When
        // Then
        assertThrows(AuthorizationFailureException.class,
                () -> authorizedOrderService.orderSummary(unauthorizedUser, testOrderId));
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthorizedForOrderStatus() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        User unauthorizedUser = new User("unauthorizedUser", "password".toCharArray());
        // When
        // Then
        assertThrows(AuthorizationFailureException.class,
                () -> authorizedOrderService.status(unauthorizedUser, testOrderId));
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthorizedForCompleteOrder() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        User unauthorizedUser = new User("unauthorizedUser", "password".toCharArray());
        // When
        // Then
        assertThrows(AuthorizationFailureException.class,
                () -> authorizedOrderService.complete(unauthorizedUser, testOrderId));
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthorizedForCancelOrder() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        User unauthorizedUser = new User("unauthorizedUser", "password".toCharArray());
        // When
        // Then
        assertThrows(AuthorizationFailureException.class,
                () -> authorizedOrderService.cancel(unauthorizedUser, testOrderId));
    }
}