package com.gamelibrary2d.functional;

/**
 * Encapsulates a method with a single parameter and no return value.
 */
public interface ParameterizedAction<T> {
    void perform(T t);
}