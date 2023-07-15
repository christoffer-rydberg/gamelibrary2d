package com.gamelibrary2d.components.denotations;

import com.gamelibrary2d.InputState;

public interface PointerMoveAware {

    /**
     * Handles pointer move events.
     *
     * @param inputState The global input state.
     * @param id           The id of the pointer.
     * @param x The x-coordinate of the pointer transformed to the coordinate space represented by the parent container.
     * @param y The y-coordinate of the pointer transformed to the coordinate space represented by the parent container.
     * @return True if the event should be swallowed and not routed to other objects, false otherwise.
     *         Other {@link PointerMoveAware} objects will be alerted of the swallowed event via {@link #swallowedPointerMove}.
     */
    boolean pointerMove(InputState inputState, int id, float x, float y);

    /**
     * Handles pointer move events that were swallowed by a previous {@link PointerMoveAware pointer move aware} object.
     *
     * @param inputState The global input state.
     * @param id The id of the pointer.
     */
    void swallowedPointerMove(InputState inputState, int id);
}