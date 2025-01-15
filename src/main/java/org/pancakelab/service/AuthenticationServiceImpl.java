package org.pancakelab.service;

import org.pancakelab.model.AuthenticationFailureException;
import org.pancakelab.model.User;

import java.util.HashSet;
import java.util.Set;

public class AuthenticationServiceImpl implements AuthenticationService {

    public static final String USER_IS_NOT_AUTHENTICATED = "User not authenticated";
    public static final String INVALID_USER = "Invalid user";

    private final HashSet<User> authenticatedUsers;

    public AuthenticationServiceImpl(Set<User> authenticatedUsers) {
        this.authenticatedUsers = new HashSet<>(authenticatedUsers);
    }

    @Override
    public void authenticate(User user) throws AuthenticationFailureException {
        if (user == null) {
            throw new AuthenticationFailureException(INVALID_USER);
        }
        if (!authenticatedUsers.contains(user)) {
            throw new AuthenticationFailureException(USER_IS_NOT_AUTHENTICATED);
        }
    }
}