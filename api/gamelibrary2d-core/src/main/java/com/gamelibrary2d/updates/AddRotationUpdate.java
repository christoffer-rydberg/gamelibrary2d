package com.gamelibrary2d.updates;

import com.gamelibrary2d.denotations.Rotatable;

/**
 * Adds the specified rotation to the {@link Rotatable} target.
 */
public class AddRotationUpdate extends AbstractUpdate {
    private final Rotatable target;
    private final float deltaRotation;

    public AddRotationUpdate(float duration, Rotatable target, float deltaRotation) {
        super(duration);
        this.target = target;
        this.deltaRotation = deltaRotation / duration;
    }

    @Override
    protected void initialize() {

    }

    @Override
    protected void onUpdate(float deltaTime) {
        target.addRotation(deltaRotation * deltaTime);
    }
}
