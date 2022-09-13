package com.gamelibrary2d.components.denotations;

import com.gamelibrary2d.common.CoordinateSpace;
import com.gamelibrary2d.common.Point;

/**
 * Represents a {@link CoordinateSpace} that be transformed.
 */
public interface Transformable extends Positionable, Rotatable, Scalable, CoordinateSpace {

    /**
     * The mutable {@link Point} representing the {@link #getScaleAndRotationAnchorX x-coordinate} and {@link #getScaleAndRotationAnchorX y-coordinate}
     * of the scale and rotation anchor.
     */
    Point getScaleAndRotationAnchor();

    /**
     * Updates the {@link #getScaleAndRotationAnchor() scale and rotation anchor} with the values from the specified {@link Point}.
     */
    default void setScaleAndRotationAnchor(Point scaleAndRotationAnchor) {
        getScaleAndRotationAnchor().set(scaleAndRotationAnchor);
    }

    default float getScaleAndRotationAnchorX() {
        return getScaleAndRotationAnchor().getX();
    }

    default float getScaleAndRotationAnchorY() {
        return getScaleAndRotationAnchor().getY();
    }

    default float getPosX() {
        return getPosition().getX();
    }

    default float getPosY() {
        return getPosition().getY();
    }

    default float getScaleX() {
        return getScale().getX();
    }

    default float getScaleY() {
        return getScale().getY();
    }

    /**
     * Updates the {@link #getScaleAndRotationAnchor() scale and rotation anchor}.
     */
    default void setScaleAndRotationAnchor(float x, float y) {
        getScaleAndRotationAnchor().set(x, y);
    }

    /**
     * Transforms the {@link #getPosition position}, {@link #getScale scale} and {@link #getRotation rotation}
     * to the specified {@link CoordinateSpace}.
     */
    default void transformTo(CoordinateSpace coordinateSpace) {
        getPosition().transformTo(coordinateSpace);
        getScale().divide(coordinateSpace.getScaleX(), coordinateSpace.getScaleY());
        setRotation(getRotation() - coordinateSpace.getRotation());
    }

    /**
     * Transforms the {@link #getPosition position}, {@link #getScale scale} and {@link #getRotation rotation}
     * from the specified {@link CoordinateSpace}.
     */
    default void transformFrom(CoordinateSpace coordinateSpace) {
        getPosition().transformFrom(coordinateSpace);
        getScale().multiply(coordinateSpace.getScaleX(), coordinateSpace.getScaleY());
        setRotation(getRotation() + coordinateSpace.getRotation());
    }

    /**
     * Updates the {@link #getPosition position}, {@link #getScale scale}, {@link #getRotation rotation}
     * and {@link #getScaleAndRotationAnchor() scale and rotation anchor}
     * with the values from the specified {@link CoordinateSpace}.
     */
    default void setCoordinateSpace(CoordinateSpace coordinateSpace) {
        setPosition(coordinateSpace.getPosX(), coordinateSpace.getPosY());
        setScale(coordinateSpace.getScaleX(), coordinateSpace.getScaleY());
        setRotation(coordinateSpace.getRotation());
        setScaleAndRotationAnchor(
                coordinateSpace.getScaleAndRotationAnchorX(),
                coordinateSpace.getScaleAndRotationAnchorY());
    }
}
