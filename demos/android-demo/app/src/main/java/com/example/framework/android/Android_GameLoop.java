package com.example.framework.android;

import com.gamelibrary2d.common.functional.Action;
import com.gamelibrary2d.common.updating.DefaultTimer;
import com.gamelibrary2d.common.updating.Timer;
import com.gamelibrary2d.common.updating.UpdateAction;
import com.gamelibrary2d.framework.GameLoop;
import com.gamelibrary2d.framework.Window;

public class Android_GameLoop implements GameLoop {
    private final Timer timer = new DefaultTimer();
    private UpdateAction updateAction;
    private Action exitAction;
    private Action disposeAction;
    private double prevTime;
    private boolean running;
    private volatile boolean paused;

    @Override
    public void initialize(UpdateAction updateAction, Action disposeAction, Window window) {
        this.updateAction = updateAction;
        this.disposeAction = disposeAction;
    }

    @Override
    public void start(Action onExit) {
        this.exitAction = onExit;
        prevTime = timer.getTime();
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        if (paused) {
            paused = false;
            prevTime = timer.getTime();
        }
    }

    boolean isRunning() {
        return running && !paused;
    }

    Action getDisposeAction() {
        return disposeAction;
    }

    Action getExitAction() {
        return exitAction;
    }

    void triggerUpdate() {
        double time = timer.getTime();
        updateAction.perform((float) (time - prevTime));
        prevTime = time;
    }
}
