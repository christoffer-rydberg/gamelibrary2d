package com.gamelibrary2d.common;

/**
 * Defines a position, scale and rotation.
 */
public interface Projection {
    float getPosX();

    float getPosY();

    float getScaleX();

    float getScaleY();

    float getRotation();

    float getScaleAndRotationCenterX();

    float getScaleAndRotationCenterY();
}
