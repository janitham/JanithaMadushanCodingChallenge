package org.pancakelab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.pancakelab.model.*;
import org.pancakelab.util.PancakeFactory;
import org.pancakelab.util.Pancakes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.pancakelab.service.AuthenticationServiceImpl.USER_IS_NOT_AUTHENTICATED;
import static org.pancakelab.util.PancakeUtils.USER_IS_NOT_AUTHORIZED;

class AuthorizedKitchenServiceTest {
    private KitchenServiceImpl kitchenService;
    private AuthenticationService authenticationService;
    private AuthorizedKitchenService authorizedKitchenService;
    private User privileged;
    private User unPrivileged;
    private User inCorrectPermissions;
    private UUID orderId;

    @BeforeEach
    public void setUp() {
        final Map<String, List<Character>> privileges = new HashMap<>() {
            {
                put("order", List.of('C', 'R', 'U', 'D'));
                put("kitchen", List.of('C', 'R', 'U', 'D'));
                put("delivery", List.of('C', 'R', 'U', 'D'));
                put("recipe", List.of('C', 'R', 'U', 'D'));
            }
        };
        kitchenService = mock(KitchenServiceImpl.class);
        authenticationService = mock(AuthenticationService.class);
        authorizedKitchenService = new AuthorizedKitchenService(kitchenService, authenticationService);
        privileged = new User("testUser", "password".toCharArray(), privileges);
        unPrivileged = new User("testUser", "password".toCharArray(), new HashMap<>());
        inCorrectPermissions = new User("testUser", "password".toCharArray(), new HashMap<>() {{
            put("delivery", List.of('C', 'R', 'U', 'D'));
        }});
        orderId = UUID.randomUUID();
    }

    @Test
    void givenAuthenticatedUser_whenViewOrders_thenReturnsOrders() throws PancakeServiceException {
        // Given
        final Map<UUID, Map<PancakeRecipe, Integer>> orders = new HashMap<>();
        when(kitchenService.viewOrders(privileged)).thenReturn(orders);
        // When
        final Map<UUID, Map<PancakeRecipe, Integer>> result = authorizedKitchenService.viewOrders(privileged);
        // Then
        verify(authenticationService).authenticate(privileged);
        verify(kitchenService).viewOrders(privileged);
        assertEquals(orders, result);
    }

    @Test
    void givenAuthenticatedUser_whenAcceptOrder_thenOrderIsAccepted() throws PancakeServiceException {
        // Given
        doNothing().when(kitchenService).acceptOrder(privileged, orderId);
        // When
        authorizedKitchenService.acceptOrder(privileged, orderId);
        // Then
        verify(authenticationService).authenticate(privileged);
        verify(kitchenService).acceptOrder(privileged, orderId);
    }

    @Test
    void givenAuthenticatedUser_whenNotifyOrderCompletion_thenOrderIsCompleted() throws PancakeServiceException {
        // Given
        doNothing().when(kitchenService).notifyOrderCompletion(privileged, orderId);
        // When
        authorizedKitchenService.notifyOrderCompletion(privileged, orderId);
        // Then
        verify(authenticationService).authenticate(privileged);
        verify(kitchenService).notifyOrderCompletion(privileged, orderId);
    }

    @Test
    void givenUnauthenticatedUser_whenViewOrders_thenThrowsException() throws AuthenticationFailureException {
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

    @ParameterizedTest
    @ValueSource(strings = {"inCorrectPermissions", "unPrivileged"})
    void givenUnprivilegedUser_whenViewOrders_thenThrowsException(String userType) {
        // Given
        final User user = userType.equals("inCorrectPermissions") ? inCorrectPermissions : unPrivileged;
        // When
        // Then
        PancakeServiceException exception = assertThrows(
                PancakeServiceException.class,
                () -> authorizedKitchenService.viewOrders(user)
        );
        assertEquals(USER_IS_NOT_AUTHORIZED, exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"inCorrectPermissions", "unPrivileged"})
    void givenUnprivilegedUser_whenAcceptOrder_thenThrowsException(String userType) {
        // Given
        final User user = userType.equals("inCorrectPermissions") ? inCorrectPermissions : unPrivileged;
        // When
        // Then
        PancakeServiceException exception = assertThrows(
                PancakeServiceException.class,
                () -> authorizedKitchenService.acceptOrder(user, orderId)
        );
        assertEquals(USER_IS_NOT_AUTHORIZED, exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"inCorrectPermissions", "unPrivileged"})
    void givenUnprivilegedUser_whenNotifyOrderCompletion_thenThrowsException(String userType) {
        // Given
        final User user = userType.equals("inCorrectPermissions") ? inCorrectPermissions : unPrivileged;
        // When
        // Then
        PancakeServiceException exception = assertThrows(
                PancakeServiceException.class,
                () -> authorizedKitchenService.notifyOrderCompletion(user, orderId)
        );
        assertEquals(USER_IS_NOT_AUTHORIZED, exception.getMessage());
    }

    @Test
    void givenAuthenticatedUser_whenAddRecipe_thenRecipeIsAdded() throws PancakeServiceException {
        // Given
        final PancakeRecipe recipe = PancakeFactory.get(Pancakes.DARK_CHOCOLATE_PANCAKE);
        doNothing().when(kitchenService).addRecipe(privileged, recipe);
        // When
        authorizedKitchenService.addRecipe(privileged, recipe);
        // Then
        verify(authenticationService).authenticate(privileged);
        verify(kitchenService).addRecipe(privileged, recipe);
    }

    @Test
    void givenAuthenticatedUser_whenRemoveRecipe_thenRecipeIsRemoved() throws PancakeServiceException {
        // Given
        final String recipe = PancakeFactory.get(Pancakes.MILK_CHOCOLATE_PANCAKE).getName();
        doNothing().when(kitchenService).removeRecipe(privileged, recipe);
        // When
        authorizedKitchenService.removeRecipe(privileged, recipe);
        // Then
        verify(authenticationService).authenticate(privileged);
        verify(kitchenService).removeRecipe(privileged, recipe);
    }

    @Test
    void givenAuthenticatedUser_whenUpdateRecipe_thenRecipeIsUpdated() throws PancakeServiceException {
        // Given
        final PancakeRecipe recipe = PancakeFactory.get(Pancakes.MILK_CHOCOLATE_PANCAKE);
        final var updated = new PancakeRecipe.Builder().withName(recipe.getName()).withChocolate(PancakeRecipe.CHOCOLATE.MILK).build();
        doNothing().when(kitchenService).updateRecipe(privileged, recipe.getName(), updated);
        // When
        authorizedKitchenService.updateRecipe(privileged, recipe.getName(), updated);
        // Then
        verify(authenticationService).authenticate(privileged);
        verify(kitchenService).updateRecipe(privileged, recipe.getName(), updated);
    }

    @Test
    void givenUnauthenticatedUser_whenAddRecipe_thenThrowsException() throws AuthenticationFailureException {
        // Given
        final PancakeRecipe recipe = PancakeFactory.get(Pancakes.DARK_CHOCOLATE_PANCAKE);
        doThrow(new AuthenticationFailureException(USER_IS_NOT_AUTHENTICATED)).when(authenticationService).authenticate(privileged);
        // When
        // Then
        PancakeServiceException exception = assertThrows(
                PancakeServiceException.class,
                () -> authorizedKitchenService.addRecipe(privileged, recipe)
        );
        assertEquals(USER_IS_NOT_AUTHENTICATED, exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"inCorrectPermissions", "unPrivileged"})
    void givenUnprivilegedUser_whenAddRecipe_thenThrowsException(String userType) {
        // Given
        final User user = userType.equals("inCorrectPermissions") ? inCorrectPermissions : unPrivileged;
        final PancakeRecipe recipe = PancakeFactory.get(Pancakes.DARK_CHOCOLATE_PANCAKE);
        // When
        // Then
        PancakeServiceException exception = assertThrows(
                PancakeServiceException.class,
                () -> authorizedKitchenService.addRecipe(user, recipe)
        );
        assertEquals(USER_IS_NOT_AUTHORIZED, exception.getMessage());
    }
}
