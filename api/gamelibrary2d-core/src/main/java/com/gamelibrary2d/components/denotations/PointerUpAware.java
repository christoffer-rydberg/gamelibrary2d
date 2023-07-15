package com.gamelibrary2d.components.denotations;

import com.gamelibrary2d.KeyAndPointerState;

public interface PointerUpAware {

    /**
     * Handles pointer up events.
     *
     * @param state The global key and pointer state.
     * @param id           The id of the pointer.
     * @param button       The id of the pointer button.
     * @param x The x-coordinate of the pointer transformed to the coordinate space represented by the parent container.
     * @param y The y-coordinate of the pointer transformed to the coordinate space represented by the parent container.
     */
    void pointerUp(KeyAndPointerState state, int id, int button, float x, float y);

}