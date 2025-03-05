package com.gamelibrary2d.interpolation;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.denotations.Updatable;

public class PointInterpolator implements Updatable {
    private final Point target;

    private float x0;
    private float y0;
    private float x1;
    private float y1;

    private float timer;
    private float duration = -1f;

    public PointInterpolator(Point target) {
        this.target = target;
    }

    public Point getTarget() {
        return target;
    }

    public void setGoal(float x, float y, float duration) {
        x0 = target.getX();
        y0 = target.getY();
        x1 = x;
        y1 = y;
        timer = 0;
        this.duration = duration;
    }

    public void abort() {
        duration = -1;
    }

    public void finish() {
        target.set(x1, y1);
        duration = -1;
    }

    @Override
    public void update(float deltaTime) {
        if (duration >= 0) {
            timer += deltaTime;
            if (timer >= duration) {
                finish();
            } else {
                target.lerp(x0, y0, x1, y1, timer / duration);
            }
        }
    }
}