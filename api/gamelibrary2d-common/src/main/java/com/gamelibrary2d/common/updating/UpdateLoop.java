package com.gamelibrary2d.common.updating;

import com.gamelibrary2d.common.denotations.Updatable;

public class UpdateLoop {
    private final Updatable target;
    private final double ups;
    private final Timer timer;

    private volatile boolean running;

    public UpdateLoop(Updatable target, double ups) {
        this.target = target;
        this.ups = ups;
        timer = new DefaultTimer();
    }

    public void run() {
        running = true;

        double prevTime = timer.getTime();
        while (!Thread.currentThread().isInterrupted() && running) {
            double startTime = timer.getTime();
            double deltaTime = startTime - prevTime;
            target.update((float) (deltaTime));
            sync(startTime);
            prevTime = startTime;
        }

        running = false;
    }

    private void sync(double startTime) {
        double timeRequired = 1.0 / ups;
        double timePassed = timer.getTime() - startTime;
        if (timePassed < timeRequired) {
            try {
                Thread.sleep((long) ((timeRequired - timePassed) * 1000));
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}