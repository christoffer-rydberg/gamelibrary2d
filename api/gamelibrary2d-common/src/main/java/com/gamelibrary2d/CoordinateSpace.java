package com.gamelibrary2d;

/**
 * Represents a position, scale and rotation in the 2D space.
 */
public interface CoordinateSpace {

    /**
     * The position along the x-axis.
     */
    float getPosX();

    /**
     * The position along the y-axis.
     */
    float getPosY();

    /**
     * The scale along the x-axis from the {@link #getScaleAndRotationAnchorX x-coordinate} of the scale and rotation anchor.
     */
    float getScaleX();

    /**
     * The scale along the y-axis from the {@link #getScaleAndRotationAnchorX y-coordinate} of the scale and rotation anchor.
     */
    float getScaleY();

    /**
     * The clockwise rotation in degrees around the {@link #getScaleAndRotationAnchorX x-coordinate} and {@link #getScaleAndRotationAnchorX y-coordinate}
     * of the scale and rotation anchor.
     */
    float getRotation();

    /**
     * The x-coordinate of the scale and rotation anchor, relative the {@link #getPosX() x-position}.
     */
    float getScaleAndRotationAnchorX();

    /**
     * The y-coordinate of the scale and rotation anchor, relative the {@link #getPosY() y-position}.
     */
    float getScaleAndRotationAnchorY();
}
