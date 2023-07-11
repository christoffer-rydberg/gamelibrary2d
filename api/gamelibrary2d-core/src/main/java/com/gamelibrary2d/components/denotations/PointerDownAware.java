package com.gamelibrary2d.components.denotations;

import com.gamelibrary2d.PointerState;

public interface PointerDownAware {

    /**
     * Handles pointer down events.
     *
     * @param pointerState The global pointer state.
     * @param id           The id of the pointer.
     * @param button       The id of the pointer button.
     * @param transformedX The x-coordinate of the pointer transformed to the coordinate space represented by the parent container.
     * @param transformedY The y-coordinate of the pointer transformed to the coordinate space represented by the parent container.
     * @return True if the event should be swallowed and not routed to other objects, false otherwise.
     */
    boolean pointerDown(PointerState pointerState, int id, int button, float transformedX, float transformedY);

}