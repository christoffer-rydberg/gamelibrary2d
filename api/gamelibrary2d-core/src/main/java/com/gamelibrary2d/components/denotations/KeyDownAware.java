package com.gamelibrary2d.components.denotations;

import com.gamelibrary2d.InputState;

public interface KeyDownAware {

    /**
     * Handles key down events.
     *
     * @param inputState The global input state.
     * @param key    The keyboard key that was pressed.
     * @param repeat True if the key action is repeat.
     */
    void keyDown(InputState inputState, int key, boolean repeat);

}
