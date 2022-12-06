package com.gamelibrary2d.components.denotations;

public interface PointerDownAware {

    /**
     * Handles pointer down actions.
     *
     * @param id           The id of the pointer.
     * @param button       The id of the pointer button.
     * @param x            The x-coordinate of the pointer.
     * @param y            The y-coordinate of the pointer.
     * @param transformedX The x-coordinate of the pointer transformed to the coordinate space represented by the parent container.
     * @param transformedY The y-coordinate of the pointer transformed to the coordinate space represented by the parent container.
     * @return True if the action is handled and should not be routed to other objects, false otherwise.
     */
    boolean pointerDown(int id, int button, float x, float y, float transformedX, float transformedY);

}