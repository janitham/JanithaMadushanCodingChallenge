package org.pancakelab.model;

public class ValidationException extends PancakeServiceException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}