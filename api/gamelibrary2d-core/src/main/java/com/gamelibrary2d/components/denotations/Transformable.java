package com.gamelibrary2d.components.denotations;

import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.Projection;

/**
 * Defines a {@link Projection} that can undergo transformations (i.e. be repositioned, scaled and resized).
 */
public interface Transformable extends Projection {

    Point getPosition();

    Point getScale();

    /**
     * @return The center point, relative to the object's position, used when
     * scaling and rotating the object.
     */
    Point getScaleAndRotationCenter();

    /**
     * @return The rotation of the object in degrees, clockwise, starting from the
     * positive y-axis.
     */
    float getRotation();

    /**
     * Sets the {@link #getRotation() rotation}.
     */
    void setRotation(float rotation);

    default void setPosition(Point position) {
        getPosition().set(position);
    }

    default void setPosition(float x, float y) {
        getPosition().set(x, y);
    }

    default void setScale(Point scale) {
        getScale().set(scale);
    }

    default void setScale(float scale) {
        getScale().set(scale, scale);
    }

    default void setScale(float x, float y) {
        getScale().set(x, y);
    }

    default void setScaleAndRotationCenter(Point scaleAndRotationCenter) {
        getScaleAndRotationCenter().set(scaleAndRotationCenter);
    }

    default void setScaleAndRotationCenter(float x, float y) {
        getScaleAndRotationCenter().set(x, y);
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

    default float getScaleAndRotationCenterX() {
        return getScaleAndRotationCenter().getX();
    }

    default float getScaleAndRotationCenterY() {
        return getScaleAndRotationCenter().getY();
    }

    default void projectTo(Projection projection) {
        getPosition().projectTo(projection);
        getScale().divide(projection.getScaleX(), projection.getScaleY());
        setRotation(getRotation() - projection.getRotation());
    }

    default void projectFrom(Projection projection) {
        getPosition().projectFrom(projection);
        getScale().multiply(projection.getScaleX(), projection.getScaleY());
        setRotation(getRotation() + projection.getRotation());
    }

    default void setProjection(Projection projection) {
        setPosition(projection.getPosX(), projection.getPosY());
        setScale(projection.getScaleX(), projection.getScaleY());
        setRotation(projection.getRotation());
    }
}
