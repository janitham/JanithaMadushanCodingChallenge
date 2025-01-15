package org.pancakelab.service;

import org.pancakelab.model.AuthenticationFailureException;
import org.pancakelab.model.User;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of the AuthenticationService interface.
 */
public class AuthenticationServiceImpl implements AuthenticationService {

    public static final String USER_IS_NOT_AUTHENTICATED = "User not authenticated";
    public static final String INVALID_USER = "Invalid user";

    private final HashSet<User> authenticatedUsers;

    /**
     * Constructs an AuthenticationServiceImpl with a set of authenticated users.
     *
     * @param authenticatedUsers users to be saved in the authentication service
     */
    public AuthenticationServiceImpl(Set<User> authenticatedUsers) {
        this.authenticatedUsers = new HashSet<>(authenticatedUsers);
    }

    /**
     * Authenticates a user.
     *
     * @param user the user to authenticate
     * @throws AuthenticationFailureException if the user is null or not authenticated
     */
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