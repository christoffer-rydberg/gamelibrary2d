package com.gamelibrary2d.denotations;

import com.gamelibrary2d.CoordinateSpace;

/**
 * Represents a {@link CoordinateSpace} that be transformed.
 */
public interface Rotatable {

    /**
     * The clockwise rotation in degrees.
     */
    float getRotation();

    /**
     * Sets the {@link #getRotation rotation}.
     */
    void setRotation(float rotation);

    /**
     * Adds to the {@link #getRotation rotation}.
     */
    default void addRotation(float rotation) {
        setRotation(getRotation() + rotation);
    }
}
