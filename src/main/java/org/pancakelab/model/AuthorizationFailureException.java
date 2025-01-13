package org.pancakelab.model;

public class AuthorizationFailureException extends PancakeServiceException {
    public AuthorizationFailureException(String message) {
        super(message);
    }

    public AuthorizationFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
