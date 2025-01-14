package org.pancakelab.model;

public interface Validator<T> {
    void validate(T object) throws ValidationException;
}
