package com.gamelibrary2d.updates;

import com.gamelibrary2d.common.denotations.Positionable;

/**
 * Moves a {@link Positionable} target to the specified position.
 */
public class PositionUpdate extends AbstractUpdate {
    private final Positionable target;
    private final float goalPosX;
    private final float goalPosY;
    private float deltaPosX;
    private float deltaPosY;

    public PositionUpdate(float duration, Positionable target, float goalPosX, float goalPosY) {
        super(duration);
        this.target = target;
        this.goalPosX = goalPosX;
        this.goalPosY = goalPosY;
    }

    @Override
    protected void initialize() {
        deltaPosX = (goalPosX - target.getPosition().getX()) / getDuration();
        deltaPosY = (goalPosY - target.getPosition().getY()) / getDuration();
    }

    @Override
    protected void onUpdate(float deltaTime) {
        target.getPosition().add(deltaPosX * deltaTime, deltaPosY * deltaTime);
    }
}
