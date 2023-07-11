package com.gamelibrary2d.components.denotations;

import com.gamelibrary2d.PointerState;

public interface PointerMoveAware {

    /**
     * Handles pointer move events.
     *
     * @param pointerState The global pointer state.
     * @param id           The id of the pointer.
     * @param transformedX The x-coordinate of the pointer transformed to the coordinate space represented by the parent container.
     * @param transformedY The y-coordinate of the pointer transformed to the coordinate space represented by the parent container.
     * @return True if the event should be swallowed and not routed to other objects, false otherwise.
     *         Other {@link PointerMoveAware} objects will be alerted of the swallowed event via {@link #swallowedPointerMove}.
     */
    boolean pointerMove(PointerState pointerState, int id, float transformedX, float transformedY);

    /**
     * Handles pointer move events that were swallowed by a previous {@link PointerMoveAware pointer move aware} object.
     *
     * @param pointerState The global pointer state.
     * @param id The id of the pointer.
     */
    void swallowedPointerMove(PointerState pointerState, int id);
}