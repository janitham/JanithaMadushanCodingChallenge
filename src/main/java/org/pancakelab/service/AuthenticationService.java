package org.pancakelab.service;

import org.pancakelab.model.AuthenticationFailureException;
import org.pancakelab.model.User;

public interface AuthenticationService {
    void authenticate(User user) throws AuthenticationFailureException;
}
