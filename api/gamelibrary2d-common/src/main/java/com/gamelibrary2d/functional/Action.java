package com.gamelibrary2d.functional;

/**
 * Encapsulates a method without parameters and no return value.
 */
public interface Action {

    Action EMPTY = () -> {
    };

    void perform();

}