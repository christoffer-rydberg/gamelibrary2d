package com.gamelibrary2d.updates;

import com.gamelibrary2d.components.denotations.Rotatable;

/**
 * Rotates a {@link Rotatable} target to the specified rotation.
 */
public class RotationUpdate extends AbstractUpdate {
    private final Rotatable target;
    private final float goalRotation;
    private float deltaRotation;

    private final boolean normalizeRotation;

    public RotationUpdate(float duration, Rotatable target, float goalRotation) {
        this(duration, target, goalRotation, false);
    }

    public RotationUpdate(float duration, Rotatable target, float goalRotation, boolean normalize) {
        super(duration);
        this.target = target;
        this.goalRotation = goalRotation;
        this.normalizeRotation = normalize;
    }

    @Override
    protected void initialize() {
        deltaRotation = goalRotation - target.getRotation();

        if (normalizeRotation) {
            deltaRotation %= 360f;
            if (deltaRotation > 180f) {
                deltaRotation -= 360f;
            } else if (deltaRotation < -180f) {
                deltaRotation += 360f;
            }
        }

        deltaRotation /= getDuration();
    }

    @Override
    protected void onUpdate(float deltaTime) {
        target.addRotation(deltaRotation * deltaTime);
    }
}
