package org.pancakelab.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.AuthenticationFailureException;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.model.User;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.pancakelab.service.AuthenticationServiceImpl.INVALID_USER;
import static org.pancakelab.service.AuthenticationServiceImpl.USER_IS_NOT_AUTHENTICATED;

public class AuthenticationServiceTest {

    private static AuthenticationService authService;

    @BeforeAll
    public static void setUp() {
        HashSet<User> predefinedUsers = new HashSet<>();
        predefinedUsers.add(new User("validUser", "validPassword".toCharArray()));
        authService = new AuthenticationServiceImpl(predefinedUsers);
    }

    @Test
    public void givenValidUser_whenAuthenticate_thenNoExceptionThrown() {
        // Given
        User validUser = new User("validUser", "validPassword".toCharArray());
        // When
        // Then
        assertDoesNotThrow(() -> authService.authenticate(validUser));
    }

    @Test
    public void givenInvalidUser_whenAuthenticate_thenExceptionThrown() {
        // Given
        User invalidUser = new User("invalidUser", "invalidPassword".toCharArray());
        // When
        // Then
        PancakeServiceException exception = assertThrows(
                AuthenticationFailureException.class, () -> authService.authenticate(invalidUser));
        assertEquals(USER_IS_NOT_AUTHENTICATED, exception.getMessage());
    }

    @Test
    public void givenNullUser_whenAuthenticate_thenExceptionThrown() {
        // Given
        // When
        // Then
        PancakeServiceException exception = assertThrows(
                PancakeServiceException.class, () -> authService.authenticate(null));
        assertEquals(INVALID_USER, exception.getMessage());
    }
}
