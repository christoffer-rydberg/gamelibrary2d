package com.gamelibrary2d.components.frames;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.components.containers.Layer;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.updaters.Updater;

public interface Frame extends Layer<Renderable>, Disposer {

    /**
     * Invoked when the frame begins.
     */
    void begin();

    /**
     * Invoked when the frame ends.
     */
    void end();

    /**
     * Pauses the game.
     */
    void pause();

    /**
     * Resumes the game if {@link #pause paused}.
     */
    void resume();

    /**
     * @return True if the frame has been {@link #pause paused}, false otherwise.
     */
    boolean isPaused();

    /**
     * Runs the specified updater until it is {@link Updater#isFinished finished}.
     * Several updaters can run in parallel, but only one instance of the same
     * {@link Updater} can run at once. Invoking this method with an {@link Updater}
     * that is already running will reset the updater if reset is specified to true.
     *
     * @param updater The updater.
     * @param reset   {@link Updater#reset} will be invoked to ensure the updater
     *                runs from the beginning, if this property is set to true.
     */
    void runUpdater(Updater updater, boolean reset);

    /**
     * Invokes {@link #runUpdater(Updater, boolean)} with reset = true.
     */
    default void runUpdater(Updater updater) {
        runUpdater(updater, true);
    }

    void invokeLater(Runnable runnable);

    Color getBackgroundColor();
}