package com.gamelibrary2d.updates;

import com.gamelibrary2d.common.denotations.Positionable;

/**
 * Adds the specified position to the {@link Positionable} target.
 */
public class AddPositionUpdate extends AbstractUpdate {
    private final Positionable target;
    private final float deltaPosX;
    private final float deltaPosY;

    public AddPositionUpdate(float duration, Positionable target, float deltaPosX, float deltaPosY) {
        super(duration);
        this.target = target;
        this.deltaPosX = deltaPosX / duration;
        this.deltaPosY = deltaPosY / duration;
    }

    @Override
    protected void initialize() {

    }

    @Override
    protected void onUpdate(float deltaTime) {
        target.getPosition().add(deltaPosX * deltaTime, deltaPosY * deltaTime);
    }
}
