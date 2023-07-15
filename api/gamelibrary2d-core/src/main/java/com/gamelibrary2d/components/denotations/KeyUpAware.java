package com.gamelibrary2d.components.denotations;

import com.gamelibrary2d.InputState;

public interface KeyUpAware {

    /**
     * Handles key up events.
     *
     * @param inputState The global input state.
     * @param key The keyboard key that was released.
     */
    void keyUp(InputState inputState, int key);

}
