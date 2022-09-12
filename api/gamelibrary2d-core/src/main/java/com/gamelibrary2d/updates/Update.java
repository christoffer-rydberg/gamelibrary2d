package com.gamelibrary2d.updates;

import com.gamelibrary2d.components.frames.Frame;

/**
 * Represents some type of activity that can span multiple update cycles.
 * It can run by invoking {@link Frame#startUpdate}.
 */
public interface Update {

    /**
     * Resets the update so that it can run again.
     */
    void reset();

    /**
     * Checks if the update is finished.
     */
    boolean isFinished();

    /**
     * Invoked on each update cycle until the update is {@link #isFinished() finished}.
     *
     * @param deltaTime The time since the last update cycle, in seconds.
     * @return The time consumed by the update, in seconds.
     */
    float update(float deltaTime);
}