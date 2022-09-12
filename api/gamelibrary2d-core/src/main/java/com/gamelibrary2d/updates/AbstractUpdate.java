package com.gamelibrary2d.updates;

public abstract class AbstractUpdate implements Update {
    private final float duration;
    private float time;

    protected AbstractUpdate(float duration) {
        this.duration = duration;
    }

    @Override
    public void reset() {
        time = 0f;
    }

    @Override
    public boolean isFinished() {
        return time >= duration;
    }

    @Override
    public float update(float deltaTime) {
        if (isFinished()) {
            return 0f;
        }

        if (time == 0f) {
            initialize();
        }

        float remainingTime = duration - time;
        if (deltaTime >= remainingTime) {
            time = duration;
            deltaTime = remainingTime;
        } else {
            time += deltaTime;
        }

        onUpdate(deltaTime);

        return deltaTime;
    }

    protected float getTime() {
        return time;
    }

    protected float getDuration() {
        return duration;
    }

    protected abstract void initialize();

    protected abstract void onUpdate(float deltaTime);
}
