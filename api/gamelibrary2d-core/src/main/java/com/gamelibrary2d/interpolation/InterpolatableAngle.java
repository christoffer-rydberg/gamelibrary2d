package com.gamelibrary2d.interpolation;

import com.gamelibrary2d.FloatUtils;
import com.gamelibrary2d.denotations.Updatable;

public class InterpolatableAngle implements Updatable {
    private float value;
    private float angle0;
    private float angle1;
    private float timer;
    private float duration = -1f;

    public InterpolatableAngle() {

    }

    public InterpolatableAngle(float value) {
        this.value = value;
    }


    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
        stopInterpolation();
    }

    public void setGoal(float goal, float duration) {
        timer = 0;
        this.duration = duration;
        angle0 = value;
        angle1 = value + FloatUtils.normalizeDegrees(goal - value);
    }

    public void stopInterpolation() {
        duration = -1;
    }

    public void finishInterpolation() {
        setValue(angle1);
    }

    @Override
    public void update(float deltaTime) {
        if (duration >= 0) {
            timer += deltaTime;
            if (timer >= duration) {
                finishInterpolation();
            } else {
                float alpha = timer / duration;
                value = angle0 * (1f - alpha) + angle1 * alpha;
            }
        }
    }
}