package com.gamelibrary2d.components.denotations;

import com.gamelibrary2d.FocusManager;

public interface PointerDownWhenFocusedAware {

    /**
     * Invoked by the {@link FocusManager} after a pointer down event,
     * even if the cursor is not over the object.
     *
     * @param id     The id of the pointer.
     * @param button The id of the pointer button.
     */
    void pointerDownWhenFocused(int id, int button);
}
