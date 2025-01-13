package org.pancakelab.service;

import org.pancakelab.model.AuthenticationFailureException;
import org.pancakelab.model.User;

import java.util.HashSet;

public class AuthenticationServiceImpl implements AuthenticationService {

    private final HashSet<User> authenticatedUsers;

    public AuthenticationServiceImpl(HashSet<User> authenticatedUsers) {
        this.authenticatedUsers = new HashSet<>(authenticatedUsers);
    }

    @Override
    public void authenticate(User user) throws AuthenticationFailureException {
        if (user == null) {
            throw new AuthenticationFailureException("Invalid user");
        }
        if (!authenticatedUsers.contains(user)) {
            throw new AuthenticationFailureException("User not authenticated");
        }
    }
}