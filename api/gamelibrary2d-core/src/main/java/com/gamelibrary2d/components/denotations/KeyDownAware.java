package com.gamelibrary2d.components.denotations;

public interface KeyDownAware {

    /**
     * Handles key down events.
     *
     * @param key    The keyboard key that was pressed.
     * @param repeat True if the key action is repeat.
     */
    void keyDown(int key, boolean repeat);

}
