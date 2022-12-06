package com.gamelibrary2d.updates;

import com.gamelibrary2d.denotations.Scalable;

/**
 * Scales a {@link Scalable} target to the specified scale.
 */
public class ScaleUpdate extends AbstractUpdate {
    private final Scalable target;
    private final float goalScaleX;
    private final float goalScaleY;
    private float deltaScaleX;
    private float deltaScaleY;

    public ScaleUpdate(float duration, Scalable target, float goalScale) {
        this(duration, target, goalScale, goalScale);
    }

    public ScaleUpdate(float duration, Scalable target, float goalScaleX, float goalScaleY) {
        super(duration);
        this.target = target;
        this.goalScaleX = goalScaleX;
        this.goalScaleY = goalScaleY;
    }

    @Override
    protected void initialize() {
        deltaScaleX = (goalScaleX - target.getScale().getX()) / getDuration();
        deltaScaleY = (goalScaleY - target.getScale().getY()) / getDuration();
    }

    @Override
    protected void onUpdate(float deltaTime) {
        target.getScale().add(deltaScaleX * deltaTime, deltaScaleY * deltaTime);
    }
}
