package com.gamelibrary2d.updates;

import com.gamelibrary2d.functional.Action;

/**
 * Represents an {@link Update} that is responsible for orchestrating other updates.
 */
public interface Updater extends Update {

    /**
     * Adds an update action that is performed in the scope of a single update cycle.
     */
    void add(Action update);

    /**
     * Adds an update that can span multiple update cycles.
     */
    void add(Update update);

    /**
     * Removes all updates.
     */
    void clear();
}
