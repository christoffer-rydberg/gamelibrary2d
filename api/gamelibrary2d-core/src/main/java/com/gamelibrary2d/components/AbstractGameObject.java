package com.gamelibrary2d.components;

import com.gamelibrary2d.FocusManager;
import com.gamelibrary2d.Point;
import com.gamelibrary2d.opengl.ModelMatrix;

public abstract class AbstractGameObject implements GameObject {
    private final Point position = new Point();
    private final Point scale = new Point(1, 1);
    private final Point scaleAndRotationAnchor = new Point();

    private float rotation;
    private float opacity = 1.0f;
    private boolean enabled = true;

    public AbstractGameObject() {

    }

    @Override
    public float getOpacity() {
        return opacity;
    }

    @Override
    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    @Override
    public Point getPosition() {
        return position;
    }

    public float getPosX() {
        return position.getX();
    }

    public float getPosY() {
        return position.getY();
    }

    @Override
    public Point getScale() {
        return scale;
    }

    @Override
    public float getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    @Override
    public Point getScaleAndRotationAnchor() {
        return scaleAndRotationAnchor;
    }

    @Override
    public void render(float alpha) {
        ModelMatrix.instance().pushMatrix();
        ModelMatrix.instance().transform(this);
        onRender(alpha * opacity);
        ModelMatrix.instance().popMatrix();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (!enabled) {
                FocusManager.unfocus(this, true);
            }
        }
    }

    protected abstract void onRender(float alpha);
}