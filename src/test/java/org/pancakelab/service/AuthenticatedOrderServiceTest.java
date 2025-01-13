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

public class AuthenticatedOrderServiceTest {

    private OrderService orderService;
    private AuthenticationService authenticationService;
    private AuthenticatedOrderService authenticatedOrderService;
    private User testUser;
    private DeliveryInfo deliveryInfo;
    private final UUID testOrderId = UUID.randomUUID();
    private final Map<PancakeMenu, Integer> testPancakes = new HashMap<>() {{
        put(PancakeMenu.DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE, 1);
        put(PancakeMenu.MILK_CHOCOLATE_PANCAKE, 2);
    }};

    @BeforeEach
    public void setUp() {
        orderService = Mockito.mock(OrderService.class);
        authenticationService = Mockito.mock(AuthenticationService.class);
        authenticatedOrderService = new AuthenticatedOrderService(orderService, authenticationService);
        testUser = new User("testUser", "password".toCharArray());
        deliveryInfo = new DeliveryInfo("1", "2");
    }

    @Test
    public void shouldAuthenticateUserWhenCreatingOrder() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(deliveryInfo, testUser)).thenReturn(testOrderId);
        // When
        authenticatedOrderService.createOrder(deliveryInfo, testUser);
        // Then
        verify(authenticationService).authenticate(testUser);
        verify(orderService).createOrder(deliveryInfo, testUser);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForCreateOrder() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException("User not authenticated")).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.createOrder(deliveryInfo, testUser));
    }

    @Test
    public void shouldAuthenticateUserWhenAddingPancakes() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(deliveryInfo, testUser)).thenReturn(testOrderId);
        authenticatedOrderService.createOrder(deliveryInfo, testUser);
        // When
        // Then
        authenticatedOrderService.addPancakes(testOrderId, testPancakes, testUser);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForAddPancakes() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException("User not authenticated")).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.addPancakes(testOrderId, testPancakes, testUser));
    }

    @Test
    public void shouldAuthenticateUserWhenGettingOrderSummary() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(deliveryInfo, testUser)).thenReturn(testOrderId);
        authenticatedOrderService.createOrder(deliveryInfo, testUser);
        // When
        when(orderService.orderSummary(testOrderId, testUser)).thenReturn(testPancakes);
        var summary = authenticatedOrderService.orderSummary(testOrderId, testUser);
        // Then
        assertNotNull(summary);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForOrderSummary() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException("User not authenticated")).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.orderSummary(testOrderId, testUser));
    }

    @Test
    public void shouldAuthenticateUserWhenGettingOrderStatus() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(deliveryInfo, testUser)).thenReturn(testOrderId);
        authenticatedOrderService.createOrder(deliveryInfo, testUser);
        // When
        authenticatedOrderService.status(testOrderId, testUser);
        // Then
        verify(orderService).status(testOrderId, testUser);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForOrderStatus() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException("User not authenticated")).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.status(testOrderId, testUser));
    }

    @Test
    public void shouldAuthenticateUserWhenCompletingOrder() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(deliveryInfo, testUser)).thenReturn(testOrderId);
        authenticatedOrderService.createOrder(deliveryInfo, testUser);
        // When
        authenticatedOrderService.complete(testOrderId, testUser);
        // Then
        verify(orderService).complete(testOrderId, testUser);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForCompleteOrder() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException("User not authenticated")).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.complete(testOrderId, testUser));
    }

    @Test
    public void shouldAuthenticateUserWhenCancellingOrder() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(deliveryInfo, testUser)).thenReturn(testOrderId);
        authenticatedOrderService.createOrder(deliveryInfo, testUser);
        // When
        authenticatedOrderService.cancel(testOrderId, testUser);
        // Then
        verify(orderService).cancel(testOrderId, testUser);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForCancelOrder() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException("User not authenticated")).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.cancel(testOrderId, testUser));
    }
}