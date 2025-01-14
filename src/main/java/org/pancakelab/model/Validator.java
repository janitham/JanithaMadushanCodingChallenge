package org.pancakelab.model;

public interface Validator<T> {
    public void validate(T object) throws ValidationException;
}
