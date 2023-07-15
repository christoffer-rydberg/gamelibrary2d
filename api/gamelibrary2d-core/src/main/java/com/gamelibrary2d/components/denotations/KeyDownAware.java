package com.gamelibrary2d.components.denotations;

import com.gamelibrary2d.KeyAndPointerState;

public interface KeyDownAware {

    /**
     * Handles key down events.
     *
     * @param state The global key and pointer state.
     * @param key    The keyboard key that was pressed.
     * @param repeat True if the key action is repeat.
     */
    void keyDown(KeyAndPointerState state, int key, boolean repeat);

}
