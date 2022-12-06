package com.example.framework;

import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.functional.Action;
import com.gamelibrary2d.updating.DefaultTimer;
import com.gamelibrary2d.updating.Timer;
import com.gamelibrary2d.GameLoop;
import com.gamelibrary2d.Window;

public class Android_GameLoop implements GameLoop {
    private final Timer timer = new DefaultTimer();
    private Updatable updateAction;
    private Action exitAction;
    private Action disposeAction;
    private double prevTime;
    private boolean running;
    private volatile boolean paused;

    @Override
    public void initialize(Updatable updateAction, Action disposeAction, Window window) {
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
        updateAction.update((float) (time - prevTime));
        prevTime = time;
    }
}
