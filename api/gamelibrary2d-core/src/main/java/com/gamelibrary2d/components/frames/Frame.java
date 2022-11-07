package com.gamelibrary2d.components.frames;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.common.functional.Action;
import com.gamelibrary2d.components.containers.Layer;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.updates.Update;

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
     * Runs the specified update.
     */
    void startUpdate(Update update);

    /**
     * Stops the specified update.
     */
    void stopUpdate(Update update);

    /**
     * Performs the specified action in the beginning of the next update cycle.
     */
    void invokeLater(Action action);
}
