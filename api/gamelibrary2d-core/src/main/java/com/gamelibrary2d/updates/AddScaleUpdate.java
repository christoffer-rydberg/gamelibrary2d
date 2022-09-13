package com.gamelibrary2d.updates;

import com.gamelibrary2d.components.denotations.Scalable;

/**
 * Adds the specified scale to the {@link Scalable} target.
 */
public class AddScaleUpdate extends AbstractUpdate {
    private final Scalable target;
    private final float deltaScaleX;
    private final float deltaScaleY;

    public AddScaleUpdate(float duration, Scalable target, float deltaScale) {
        this(duration, target, deltaScale, deltaScale);
    }

    public AddScaleUpdate(float duration, Scalable target, float deltaScaleX, float deltaScaleY) {
        super(duration);
        this.target = target;
        this.deltaScaleX = deltaScaleX / duration;
        this.deltaScaleY = deltaScaleY / duration;
    }

    @Override
    protected void initialize() {

    }

    @Override
    protected void onUpdate(float deltaTime) {
        target.getScale().add(deltaScaleX * deltaTime, deltaScaleY * deltaTime);
    }
}
