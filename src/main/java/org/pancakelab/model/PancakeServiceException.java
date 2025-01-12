package org.pancakelab.model;

public class PancakeServiceException extends Exception {
    public PancakeServiceException(String message) {
        super(message);
    }

    public PancakeServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
