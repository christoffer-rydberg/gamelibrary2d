package com.gamelibrary2d.common.denotations;

import com.gamelibrary2d.common.CoordinateSpace;
import com.gamelibrary2d.common.Point;

/**
 * Represents a {@link CoordinateSpace} that be transformed.
 */
public interface Scalable {

    /**
     * The mutable scale.
     */
    Point getScale();

    /**
     * Updates the {@link #getScale scale} with the values from the specified {@link Point}.
     */
    default void setScale(Point scale) {
        getScale().set(scale);
    }

    /**
     * Updates the {@link #getScale scale} with the same value for all axes.
     */
    default void setScale(float scale) {
        getScale().set(scale, scale);
    }

    /**
     * Updates the {@link #getScale scale}.
     */
    default void setScale(float x, float y) {
        getScale().set(x, y);
    }
}
