package org.pancakelab.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.AuthenticationFailureException;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.User;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.pancakelab.service.AuthenticationServiceImpl.INVALID_USER;
import static org.pancakelab.service.AuthenticationServiceImpl.USER_IS_NOT_AUTHENTICATED;

public class AuthenticationServiceTest {

    private static AuthenticationService authService;

    private static final Map<String, List<Character>> privileges = new HashMap<>() {
        {
            put("order", List.of('C', 'R', 'U', 'D'));
            put("kitchen", List.of('C', 'R', 'U', 'D'));
            put("delivery", List.of('C', 'R', 'U', 'D'));
        }
    };

    @BeforeAll
    public static void setUp() {
        final HashSet<User> predefinedUsers = new HashSet<>();
        predefinedUsers.add(new User("validUser", "validPassword".toCharArray(), privileges));
        authService = new AuthenticationServiceImpl(predefinedUsers);
    }

    @Test
    void givenValidUser_whenAuthenticate_thenNoExceptionThrown() {
        // Given
        final User validUser = new User("validUser", "validPassword".toCharArray(), privileges);
        // When
        // Then
        assertDoesNotThrow(() -> authService.authenticate(validUser));
    }

    @Test
    void givenInvalidUser_whenAuthenticate_thenExceptionThrown() {
        // Given
        final User invalidUser = new User("invalidUser", "invalidPassword".toCharArray(), privileges);
        // When
        // Then
        PancakeServiceException exception = assertThrows(
                AuthenticationFailureException.class, () -> authService.authenticate(invalidUser));
        assertEquals(USER_IS_NOT_AUTHENTICATED, exception.getMessage());
    }

    @Test
    void givenNullUser_whenAuthenticate_thenExceptionThrown() {
        // Given
        // When
        // Then
        PancakeServiceException exception = assertThrows(
                PancakeServiceException.class, () -> authService.authenticate(null));
        assertEquals(INVALID_USER, exception.getMessage());
    }
}
