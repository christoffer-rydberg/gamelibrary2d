package com.gamelibrary2d.denotations;

import com.gamelibrary2d.Point;

public interface Positionable {

    /**
     * The mutable position.
     */
    Point getPosition();

    /**
     * Updates the {@link #getPosition position} with the values from the specified {@link Point}.
     */
    default void setPosition(Point position) {
        getPosition().set(position);
    }

    /**
     * Updates the {@link #getPosition position} with the specified values.
     */
    default void setPosition(float x, float y) {
        getPosition().set(x, y);
    }
}
