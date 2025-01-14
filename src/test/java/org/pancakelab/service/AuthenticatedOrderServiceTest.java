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

public class AuthenticatedOrderServiceTest {

    private OrderService orderService;
    private AuthenticationService authenticationService;
    private AuthenticatedOrderService authenticatedOrderService;
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
        authenticatedOrderService = new AuthenticatedOrderService(orderService, authenticationService);
        deliveryInfo = new DeliveryInfo("1", "2");
    }

    @Test
    public void shouldAuthenticateUserWhenCreatingOrder() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        // When
        authenticatedOrderService.createOrder(testUser, deliveryInfo);
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
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.createOrder(testUser, deliveryInfo));
    }

    @Test
    public void shouldAuthenticateUserWhenAddingPancakes() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authenticatedOrderService.createOrder(testUser, deliveryInfo);
        // When
        authenticatedOrderService.addPancakes(testUser, testOrderId, testPancakes);
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
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.addPancakes(testUser, testOrderId, testPancakes));
    }

    @Test
    public void shouldAuthenticateUserWhenGettingOrderSummary() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authenticatedOrderService.createOrder(testUser, deliveryInfo);
        // When
        when(orderService.orderSummary(testUser, testOrderId)).thenReturn(testPancakes);
        var summary = authenticatedOrderService.orderSummary(testUser, testOrderId);
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
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.orderSummary(testUser, testOrderId));
    }

    @Test
    public void shouldAuthenticateUserWhenGettingOrderStatus() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authenticatedOrderService.createOrder(testUser, deliveryInfo);
        // When
        authenticatedOrderService.status(testUser, testOrderId);
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
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.status(testUser, testOrderId));
    }

    @Test
    public void shouldAuthenticateUserWhenCompletingOrder() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authenticatedOrderService.createOrder(testUser, deliveryInfo);
        // When
        authenticatedOrderService.complete(testUser, testOrderId);
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
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.complete(testUser, testOrderId));
    }

    @Test
    public void shouldAuthenticateUserWhenCancellingOrder() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authenticatedOrderService.createOrder(testUser, deliveryInfo);
        // When
        authenticatedOrderService.cancel(testUser, testOrderId);
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
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.cancel(testUser, testOrderId));
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthorizedForAddPancakes() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authenticatedOrderService.createOrder(testUser, deliveryInfo);
        User unauthorizedUser = new User("unauthorizedUser", "password".toCharArray());
        // When
        // Then
        assertThrows(AuthorizationFailureException.class,
                () -> authenticatedOrderService.addPancakes(unauthorizedUser, testOrderId, testPancakes));
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthorizedForOrderSummary() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authenticatedOrderService.createOrder(testUser, deliveryInfo);
        User unauthorizedUser = new User("unauthorizedUser", "password".toCharArray());
        // When
        // Then
        assertThrows(AuthorizationFailureException.class,
                () -> authenticatedOrderService.orderSummary(unauthorizedUser, testOrderId));
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthorizedForOrderStatus() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authenticatedOrderService.createOrder(testUser, deliveryInfo);
        User unauthorizedUser = new User("unauthorizedUser", "password".toCharArray());
        // When
        // Then
        assertThrows(AuthorizationFailureException.class,
                () -> authenticatedOrderService.status(unauthorizedUser, testOrderId));
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthorizedForCompleteOrder() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authenticatedOrderService.createOrder(testUser, deliveryInfo);
        User unauthorizedUser = new User("unauthorizedUser", "password".toCharArray());
        // When
        // Then
        assertThrows(AuthorizationFailureException.class,
                () -> authenticatedOrderService.complete(unauthorizedUser, testOrderId));
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthorizedForCancelOrder() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authenticatedOrderService.createOrder(testUser, deliveryInfo);
        User unauthorizedUser = new User("unauthorizedUser", "password".toCharArray());
        // When
        // Then
        assertThrows(AuthorizationFailureException.class,
                () -> authenticatedOrderService.cancel(unauthorizedUser, testOrderId));
    }
}