package com.gamelibrary2d.functional;

/**
 * Encapsulates a method with a return value and no parameters.
 */
public interface Factory<T> {

    T create();

}