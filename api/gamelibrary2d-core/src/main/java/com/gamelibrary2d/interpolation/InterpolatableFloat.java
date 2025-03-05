package com.gamelibrary2d.interpolation;

import com.gamelibrary2d.denotations.Updatable;

public class InterpolatableFloat implements Updatable {
    private float value;
    private float value0;
    private float value1;
    private float timer;
    private float duration = -1f;

    public InterpolatableFloat() {

    }

    public InterpolatableFloat(float value) {
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
        value0 = value;
        value1 = goal;
    }

    public void stopInterpolation() {
        duration = -1;
    }

    public void finishInterpolation() {
        setValue(value1);
    }

    @Override
    public void update(float deltaTime) {
        if (duration >= 0) {
            timer += deltaTime;
            if (timer >= duration) {
                finishInterpolation();
            } else {
                float alpha = timer / duration;
                value = value0 * (1f - alpha) + value1 * alpha;
            }
        }
    }
}