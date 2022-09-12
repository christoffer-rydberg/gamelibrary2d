package com.gamelibrary2d.updates;

/**
 * Represents an {@link Update} that does nothing for a specified duration.
 * This is useful in combination with a {@link SequentialUpdater} in order to set a delay between updates.
 */
public class IdleUpdate extends AbstractUpdate {

    public IdleUpdate(float duration) {
        super(duration);
    }

    @Override
    protected void initialize() {

    }

    @Override
    protected void onUpdate(float deltaTime) {

    }
}