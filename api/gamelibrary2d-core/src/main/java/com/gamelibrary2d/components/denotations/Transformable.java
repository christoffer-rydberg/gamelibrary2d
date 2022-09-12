package com.gamelibrary2d.components.denotations;

import com.gamelibrary2d.common.CoordinateSpace;
import com.gamelibrary2d.common.Point;

/**
 * Represents a {@link CoordinateSpace} that be transformed.
 */
public interface Transformable extends CoordinateSpace {

    /**
     * The mutable {@link Point} representing the {@link #getPosX x-position} and {@link #getPosY y-position}
     */
    Point getPosition();

    /**
     * The mutable {@link Point} representing the {@link #getScaleX x-axis scale} and {@link #getScaleY y-axis scale}
     */
    Point getScale();

    /**
     * The mutable {@link Point} representing the {@link #getScaleAndRotationAnchorX x-coordinate} and {@link #getScaleAndRotationAnchorX y-coordinate}
     * of the scale and rotation anchor.
     */
    Point getScaleAndRotationAnchor();

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

    /**
     * Updates the {@link #getPosition position} by adding the values from the specified {@link Point}.
     */
    default void addPosition(Point position) {
        getPosition().add(position);
    }

    /**
     * Updates the {@link #getPosition position} by adding the specified values.
     */
    default void addPosition(float x, float y) {
        getPosition().add(x, y);
    }

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

    /**
     * Updates the {@link #getScale scale} by adding the values from the specified {@link Point}.
     */
    default void addScale(Point scale) {
        getScale().add(scale);
    }

    /**
     * Updates the {@link #getScale scale} by adding the same value for all axes.
     */
    default void addScale(float scale) {
        getScale().add(scale, scale);
    }

    /**
     * Updates the {@link #getScale scale} by adding the specified values.
     */
    default void addScale(float x, float y) {
        getScale().add(x, y);
    }

    /**
     * Updates the {@link #getScaleAndRotationAnchor() scale and rotation anchor} with the values from the specified {@link Point}.
     */
    default void setScaleAndRotationAnchor(Point scaleAndRotationAnchor) {
        getScaleAndRotationAnchor().set(scaleAndRotationAnchor);
    }

    /**
     * Updates the {@link #getScaleAndRotationAnchor() scale and rotation anchor}.
     */
    default void setScaleAndRotationAnchor(float x, float y) {
        getScaleAndRotationAnchor().set(x, y);
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

    default float getScaleAndRotationAnchorX() {
        return getScaleAndRotationAnchor().getX();
    }

    default float getScaleAndRotationAnchorY() {
        return getScaleAndRotationAnchor().getY();
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
