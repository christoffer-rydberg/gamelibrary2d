package com.gamelibrary2d.interpolation;

import com.gamelibrary2d.denotations.Transformable;
import com.gamelibrary2d.denotations.Updatable;

public class RotationInterpolator implements Updatable {
    private final Transformable target;
    private final InterpolatableAngle rotation;

    public RotationInterpolator(Transformable target) {
        this.target = target;
        rotation = new InterpolatableAngle(target.getRotation());
    }

    public void setGoal(float goal, float time) {
        rotation.setAngle(target.getRotation());
        rotation.beginInterpolation(goal, time);
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
        target.setRotation(rotation.getAngle());
    }
}