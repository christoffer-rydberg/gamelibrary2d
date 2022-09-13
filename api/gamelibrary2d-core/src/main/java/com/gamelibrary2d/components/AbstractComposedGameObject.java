package com.gamelibrary2d.components;

import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.framework.Renderable;

public abstract class AbstractComposedGameObject<T extends GameObject> implements GameObject {
    protected abstract T getComposition();

    @Override
    public void render(float alpha) {
        getComposition().render(alpha);
    }

    @Override
    public Renderable getRenderer() {
        return getComposition().getRenderer();
    }

    @Override
    public Rectangle getBounds() {
        return getComposition().getBounds();
    }

    @Override
    public Point getPosition() {
        return getComposition().getPosition();
    }

    @Override
    public Point getScale() {
        return getComposition().getScale();
    }

    @Override
    public Point getScaleAndRotationAnchor() {
        return getComposition().getScaleAndRotationAnchor();
    }

    @Override
    public float getRotation() {
        return getComposition().getRotation();
    }

    @Override
    public void setRotation(float rotation) {
        getComposition().setRotation(rotation);
    }

    @Override
    public boolean isEnabled() {
        return getComposition().isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        getComposition().setEnabled(enabled);
    }

    @Override
    public float getOpacity() {
        return getComposition().getOpacity();
    }

    @Override
    public void setOpacity(float opacity) {
        getComposition().setOpacity(opacity);
    }
}
