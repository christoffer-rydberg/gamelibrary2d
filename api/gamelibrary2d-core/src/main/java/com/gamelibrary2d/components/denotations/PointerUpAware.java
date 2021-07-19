package com.gamelibrary2d.components.denotations;

public interface PointerUpAware {

    /**
     * Handles pointer up actions.
     *
     * @param id         The id of the pointer.
     * @param button     The id of the pointer button.
     * @param x          The x-coordinate of the pointer.
     * @param y          The y-coordinate of the pointer.
     * @param projectedX The x-coordinate of the pointer projected to the parent container.
     * @param projectedY The y-coordinate of the pointer projected to the parent container.
     */
    void pointerUp(int id, int button, float x, float y, float projectedX, float projectedY);

}