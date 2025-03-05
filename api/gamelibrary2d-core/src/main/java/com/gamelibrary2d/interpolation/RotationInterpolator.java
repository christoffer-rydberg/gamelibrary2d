package com.gamelibrary2d.interpolation;

import com.gamelibrary2d.denotations.Rotatable;
import com.gamelibrary2d.denotations.Updatable;

public class RotationInterpolator implements Updatable {
    private final Rotatable target;
    private final InterpolatableAngle rotation;

    public RotationInterpolator(Rotatable target) {
        this.target = target;
        rotation = new InterpolatableAngle(target.getRotation());
    }

    public Rotatable getTarget() {
        return target;
    }

    public void setGoal(float goal, float duration) {
        rotation.setValue(target.getRotation());
        rotation.setGoal(goal, duration);
    }

    public void abort() {
        rotation.stopInterpolation();
    }

    public void finish() {
        rotation.finishInterpolation();
    }

    @Override
    public void update(float deltaTime) {
        rotation.update(deltaTime);
        target.setRotation(rotation.getValue());
    }
}