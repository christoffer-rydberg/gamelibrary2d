package com.gamelibrary2d.components.denotations;

public interface PointerUpAware {

    /**
     * Handles pointer up actions.
     *
     * @param id           The id of the pointer.
     * @param button       The id of the pointer button.
     * @param x            The x-coordinate of the pointer.
     * @param y            The y-coordinate of the pointer.
     * @param transformedX The x-coordinate of the pointer transformed to the coordinate space represented by the parent container.
     * @param transformedY The y-coordinate of the pointer transformed to the coordinate space represented by the parent container.
     */
    void pointerUp(int id, int button, float x, float y, float transformedX, float transformedY);

}