package com.gamelibrary2d;

import com.gamelibrary2d.components.frames.Frame;
import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.event.EventListener;
import com.gamelibrary2d.functional.Action;

import java.io.IOException;

public interface Game extends Disposer, Updatable {

    /**
     * Starts the game inside the specified {@link Window}.
     */
    void start(Window window, GameLoop gameLoop) throws IOException;

    default void start(Window window) throws IOException {
        start(window, Runtime.getFramework().createDefaultGameLoop());
    }

    Window getWindow();

    void setViewPort(int x, int y, int width, int height);

    void render();

    /**
     * Called each update tick in order to update and render the current frame.
     *
     * @param delta - Time since prior update.
     */
    @Override
    void update(float delta);

    /**
     * Performs the specified action in the beginning of the next update cycle.
     */
    void invokeLater(Action action);

    /**
     * Sets the specified frame.
     *
     * @param frame           - The new frame.
     * @param disposePrevious - Disposal of previous frame.
     */
    void setFrame(Frame frame, boolean disposePrevious);

    /**
     * Gets the current frame.
     */
    Frame getFrame();

    /**
     * @param id - The pointer id.
     * @return - True if the pointer is within the game area..
     */
    boolean hasPointerFocus(int id);

    /**
     * Exists the game after the current update cycle.
     */
    void exit();

    /**
     * The frame changed event occurs when a frame has been set, right before {@link Frame#begin()} is invoked.
     */
    void addFrameChangedListener(FrameChangedListener listener);

    /**
     * Removes the specified {@link #addFrameChangedListener frame changed} event listener.
     */
    void removeFrameChangedListener(FrameChangedListener listener);

    interface FrameChangedListener extends EventListener<Frame> {

    }
}