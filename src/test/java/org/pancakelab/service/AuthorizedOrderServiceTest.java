package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.pancakelab.model.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.pancakelab.service.AuthenticationServiceImpl.USER_IS_NOT_AUTHENTICATED;

class AuthorizedOrderServiceTest {

    private OrderService orderService;
    private AuthenticationService authenticationService;
    private AuthorizedOrderService authorizedOrderService;
    private User testUser;
    private User unPrivileged;
    private User inCorrectPermissions;
    private DeliveryInfo deliveryInfo;
    private final UUID testOrderId = UUID.randomUUID();
    private final Map<Pancakes, Integer> testPancakes = new EnumMap<>(Pancakes.class) {
    {
        put(Pancakes.DARK_CHOCOLATE_WHIP_CREAM_HAZELNUTS_PANCAKE, 1);
        put(Pancakes.MILK_CHOCOLATE_PANCAKE, 2);
    }
};
    private final Map<String, List<Character>> privileges = new HashMap<>() {
        {
            put("order", List.of('C', 'R', 'U', 'D'));
            put("kitchen", List.of('C', 'R', 'U', 'D'));
            put("delivery", List.of('C', 'R', 'U', 'D'));
        }
    };

    @BeforeEach
    public void setUp() {
        testUser = new User("testUser", "password".toCharArray(), privileges);
        inCorrectPermissions = new User("testUser", "password".toCharArray(), new HashMap<>() {{
            put("delivery", List.of('C', 'R', 'U', 'D'));
        }});
        unPrivileged = new User("testUser", "password".toCharArray(), new HashMap<>());
        authenticationService = Mockito.mock(AuthenticationService.class);
        orderService = Mockito.mock(OrderService.class);
        authorizedOrderService = new AuthorizedOrderService(orderService, authenticationService);
        deliveryInfo = new DeliveryInfo("1", "2");
    }

    @Test
    void shouldAuthenticateUserWhenCreatingOrder() throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        // When
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        // Then
        verify(authenticationService).authenticate(testUser);
        verify(orderService).createOrder(testUser, deliveryInfo);
    }

    @Test
    void shouldThrowExceptionWhenUserNotAuthenticatedForCreateOrder() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException(USER_IS_NOT_AUTHENTICATED)).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authorizedOrderService.createOrder(testUser, deliveryInfo));
    }

    @Test
    void shouldAuthenticateUserWhenAddingPancakes() throws PancakeServiceException {
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
    void shouldThrowExceptionWhenUserNotAuthenticatedForAddPancakes() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException(USER_IS_NOT_AUTHENTICATED)).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authorizedOrderService.addPancakes(testUser, testOrderId, testPancakes));
    }

    @Test
    void shouldAuthenticateUserWhenGettingOrderSummary() throws PancakeServiceException {
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
    void shouldThrowExceptionWhenUserNotAuthenticatedForOrderSummary() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException(USER_IS_NOT_AUTHENTICATED)).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authorizedOrderService.orderSummary(testUser, testOrderId));
    }

    @Test
    void shouldAuthenticateUserWhenGettingOrderStatus() throws PancakeServiceException {
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
    void shouldThrowExceptionWhenUserNotAuthenticatedForOrderStatus() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException(USER_IS_NOT_AUTHENTICATED)).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authorizedOrderService.status(testUser, testOrderId));
    }

    @Test
    void shouldAuthenticateUserWhenCompletingOrder() throws PancakeServiceException {
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
    void shouldThrowExceptionWhenUserNotAuthenticatedForCompleteOrder() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException(USER_IS_NOT_AUTHENTICATED)).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authorizedOrderService.complete(testUser, testOrderId));
    }

    @Test
    void shouldAuthenticateUserWhenCancellingOrder() throws PancakeServiceException {
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
    void shouldThrowExceptionWhenUserNotAuthenticatedForCancelOrder() throws PancakeServiceException {
        // Given
        // When
        // Then
        doThrow(new AuthenticationFailureException(USER_IS_NOT_AUTHENTICATED)).when(authenticationService).authenticate(testUser);
        assertThrows(AuthenticationFailureException.class, () -> authorizedOrderService.cancel(testUser, testOrderId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"inCorrectPermissions", "unPrivileged"})
    void shouldThrowExceptionWhenUserNotAuthorizedForAddPancakes(String userType) throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        User user = userType.equals("inCorrectPermissions") ? inCorrectPermissions : unPrivileged;
        // When
        // Then
        assertThrows(AuthorizationFailureException.class,
                () -> authorizedOrderService.addPancakes(user, testOrderId, testPancakes));
    }

    @ParameterizedTest
    @ValueSource(strings = {"inCorrectPermissions", "unPrivileged"})
    void shouldThrowExceptionWhenUserNotAuthorizedForOrderSummary(String userType) throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        User user = userType.equals("inCorrectPermissions") ? inCorrectPermissions : unPrivileged;
        // When
        // Then
        assertThrows(AuthorizationFailureException.class,
                () -> authorizedOrderService.orderSummary(user, testOrderId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"inCorrectPermissions", "unPrivileged"})
    void shouldThrowExceptionWhenUserNotAuthorizedForOrderStatus(String userType) throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        User user = userType.equals("inCorrectPermissions") ? inCorrectPermissions : unPrivileged;
        // When
        // Then
        assertThrows(AuthorizationFailureException.class,
                () -> authorizedOrderService.status(user, testOrderId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"inCorrectPermissions", "unPrivileged"})
    void shouldThrowExceptionWhenUserNotAuthorizedForCompleteOrder(String userType) throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        User user = userType.equals("inCorrectPermissions") ? inCorrectPermissions : unPrivileged;
        // When
        // Then
        assertThrows(AuthorizationFailureException.class,
                () -> authorizedOrderService.complete(user, testOrderId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"inCorrectPermissions", "unPrivileged"})
    void shouldThrowExceptionWhenUserNotAuthorizedForCancelOrder(String userType) throws PancakeServiceException {
        // Given
        when(orderService.createOrder(testUser, deliveryInfo)).thenReturn(testOrderId);
        authorizedOrderService.createOrder(testUser, deliveryInfo);
        User user = userType.equals("inCorrectPermissions") ? inCorrectPermissions : unPrivileged;
        // When
        // Then
        assertThrows(AuthorizationFailureException.class,
                () -> authorizedOrderService.cancel(user, testOrderId));
    }
}