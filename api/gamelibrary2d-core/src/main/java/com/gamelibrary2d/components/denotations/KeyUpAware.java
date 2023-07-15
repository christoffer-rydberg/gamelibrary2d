package com.gamelibrary2d.components.denotations;

import com.gamelibrary2d.KeyAndPointerState;

public interface KeyUpAware {

    /**
     * Handles key up events.
     *
     * @param state The global key and pointer state.
     * @param key The keyboard key that was released.
     */
    void keyUp(KeyAndPointerState state, int key);

}
