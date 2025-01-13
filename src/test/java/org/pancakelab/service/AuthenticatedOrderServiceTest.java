package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pancakelab.model.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
        authenticatedOrderService.createOrder(deliveryInfo, testUser);
        verify(authenticationService).authenticate(testUser);
        verify(orderService).createOrder(deliveryInfo, testUser);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForCreateOrder() throws PancakeServiceException {
        doThrow(new AuthenticationFailureException("User not authenticated")).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.createOrder(deliveryInfo, testUser));
    }

    @Test
    public void shouldAuthenticateUserWhenAddingPancakes() throws PancakeServiceException {
        authenticatedOrderService.addPancakes(testOrderId, testPancakes, testUser);
        verify(authenticationService).authenticate(testUser);
        verify(orderService).addPancakes(testOrderId, testPancakes, testUser);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForAddPancakes() throws PancakeServiceException {
        doThrow(new AuthenticationFailureException("User not authenticated")).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.addPancakes(testOrderId, testPancakes, testUser));
    }

    @Test
    public void shouldAuthenticateUserWhenGettingOrderSummary() throws PancakeServiceException {
        authenticatedOrderService.orderSummary(testOrderId, testUser);
        verify(authenticationService).authenticate(testUser);
        verify(orderService).orderSummary(testOrderId, testUser);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForOrderSummary() throws PancakeServiceException {
        doThrow(new AuthenticationFailureException("User not authenticated")).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.orderSummary(testOrderId, testUser));
    }

    @Test
    public void shouldAuthenticateUserWhenGettingOrderStatus() throws PancakeServiceException {
        authenticatedOrderService.status(testOrderId, testUser);
        verify(authenticationService).authenticate(testUser);
        verify(orderService).status(testOrderId, testUser);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForOrderStatus() throws PancakeServiceException {
        doThrow(new AuthenticationFailureException("User not authenticated")).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.status(testOrderId, testUser));
    }

    @Test
    public void shouldAuthenticateUserWhenCompletingOrder() throws PancakeServiceException {
        authenticatedOrderService.complete(testOrderId, testUser);
        verify(authenticationService).authenticate(testUser);
        verify(orderService).complete(testOrderId, testUser);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForCompleteOrder() throws PancakeServiceException {
        doThrow(new AuthenticationFailureException("User not authenticated")).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.complete(testOrderId, testUser));
    }

    @Test
    public void shouldAuthenticateUserWhenCancellingOrder() throws PancakeServiceException {
        authenticatedOrderService.cancel(testOrderId, testUser);
        verify(authenticationService).authenticate(testUser);
        verify(orderService).cancel(testOrderId, testUser);
    }

    @Test
    public void shouldThrowExceptionWhenUserNotAuthenticatedForCancelOrder() throws PancakeServiceException {
        doThrow(new AuthenticationFailureException("User not authenticated")).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authenticatedOrderService.cancel(testOrderId, testUser));
    }
}
