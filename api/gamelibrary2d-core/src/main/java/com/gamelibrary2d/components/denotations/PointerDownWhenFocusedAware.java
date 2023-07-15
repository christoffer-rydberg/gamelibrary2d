package com.gamelibrary2d.components.denotations;

import com.gamelibrary2d.FocusManager;
import com.gamelibrary2d.InputState;

public interface PointerDownWhenFocusedAware {

    /**
     * Invoked by the {@link FocusManager} after a pointer down event,
     * even if the cursor is not over the object.
     *
     * @param inputState The global input state.
     * @param id           The id of the pointer.
     * @param button       The id of the pointer button.
     */
    void pointerDownWhenFocused(InputState inputState, int id, int button);
}
