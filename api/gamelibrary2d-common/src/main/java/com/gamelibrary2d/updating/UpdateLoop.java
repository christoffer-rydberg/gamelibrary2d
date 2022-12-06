package com.gamelibrary2d.updating;

import com.gamelibrary2d.denotations.Updatable;

public class UpdateLoop {
    private final Timer timer = new DefaultTimer();
    private volatile boolean running;

    public void run(double ups, Updatable target) {
        running = true;

        double prevTime = timer.getTime();
        while (!Thread.currentThread().isInterrupted() && running) {
            double startTime = timer.getTime();
            double deltaTime = startTime - prevTime;
            target.update((float) (deltaTime));
            sync(startTime, ups);
            prevTime = startTime;
        }

        running = false;
    }

    private void sync(double startTime, double ups) {
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