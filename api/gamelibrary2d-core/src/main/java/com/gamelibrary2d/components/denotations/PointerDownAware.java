package com.gamelibrary2d.components.denotations;

import com.gamelibrary2d.InputState;

public interface PointerDownAware {

    /**
     * Handles pointer down events.
     *
     * @param inputState The global input state.
     * @param id           The id of the pointer.
     * @param button       The id of the pointer button.
     * @param x The x-coordinate of the pointer transformed to the coordinate space represented by the parent container.
     * @param y The y-coordinate of the pointer transformed to the coordinate space represented by the parent container.
     * @return True if the event should be swallowed and not routed to other objects, false otherwise.
     */
    boolean pointerDown(InputState inputState, int id, int button, float x, float y);

}