package com.gamelibrary2d.updates;

import com.gamelibrary2d.components.denotations.Opacifiable;

/**
 * Updates opacity of an {@link Opacifiable} target to the specified opacity.
 */
public class OpacityUpdate extends AbstractUpdate {
    private final Opacifiable target;
    private final float goalOpacity;
    private float deltaOpacity;

    public OpacityUpdate(float duration, Opacifiable target, float goalOpacity) {
        super(duration);
        this.target = target;
        this.goalOpacity = goalOpacity;
    }

    @Override
    protected void initialize() {
        deltaOpacity = (goalOpacity - target.getOpacity()) / getDuration();
    }

    @Override
    protected void onUpdate(float deltaTime) {
        target.addOpacity(deltaOpacity * deltaTime);
    }
}
