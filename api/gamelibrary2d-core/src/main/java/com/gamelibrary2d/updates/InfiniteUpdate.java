package com.gamelibrary2d.updates;

import com.gamelibrary2d.denotations.Updatable;

public class InfiniteUpdate implements Update {
    private final Updatable onUpdate;
    private boolean aborted;

    public InfiniteUpdate(Updatable onUpdate) {
        this.onUpdate = onUpdate;
    }

    @Override
    public void reset() {
        aborted = false;
    }

    @Override
    public boolean isFinished() {
        return aborted;
    }

    @Override
    public float update(float deltaTime) {
        onUpdate.update(deltaTime);
        return deltaTime;
    }
}