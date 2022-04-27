package com.gamelibrary2d.components.frames;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.common.functional.Action;
import com.gamelibrary2d.components.containers.Layer;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.updaters.Updater;

public interface Frame extends Layer<Renderable>, Disposer {

    /**
     * The background color of the frame.
     */
    Color getBackgroundColor();

    /**
     * Invoked when the frame begins.
     */
    void begin();

    /**
     * Invoked when the frame ends.
     */
    void end();

    /**
     * Starts the specified updater.
     */
    void startUpdater(Updater updater);

    /**
     * Stops the specified updater.
     */
    void stopUpdater(Updater updater);

    /**
     * Performs the specified action after the current update cycle.
     */
    void invokeLater(Action runnable);
}
