package com.gamelibrary2d.splitscreen;

import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.components.denotations.Transformable;

class InternalTargetSettings implements Transformable {
    private final Point position = new Point();
    private final Point scale = new Point(1f, 1f);
    private final Point scaleAndRotationAnchor = new Point();
    private float rotation;

    @Override
    public Point getPosition() {
        return position;
    }

    @Override
    public Point getScale() {
        return scale;
    }

    @Override
    public Point getScaleAndRotationAnchor() {
        return scaleAndRotationAnchor;
    }

    @Override
    public float getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }
}
