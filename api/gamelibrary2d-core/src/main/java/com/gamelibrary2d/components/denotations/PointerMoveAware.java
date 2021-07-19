package com.gamelibrary2d.components.denotations;

public interface PointerMoveAware {

    /**
     * Handles pointer move actions.
     *
     * @param id         The id of the pointer.
     * @param x          The x-coordinate of the pointer.
     * @param y          The y-coordinate of the pointer.
     * @param projectedX The x-coordinate of the pointer projected to the parent container.
     * @param projectedY The y-coordinate of the pointer projected to the parent container.
     * @return True if the action is handled and should not be routed to other objects, false otherwise.
     */
    boolean pointerMove(int id, float x, float y, float projectedX, float projectedY);

}