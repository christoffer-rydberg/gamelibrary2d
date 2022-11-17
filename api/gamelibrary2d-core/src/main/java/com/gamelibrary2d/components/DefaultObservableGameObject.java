package com.gamelibrary2d.components;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.denotations.Bounded;
import com.gamelibrary2d.framework.Renderable;

public final class DefaultObservableGameObject<T extends Renderable> extends AbstractObservableGameObject {
    private T renderer;
    private Rectangle bounds;

    public DefaultObservableGameObject() {

    }

    public DefaultObservableGameObject(T renderer) {
        this.renderer = renderer;
    }

    @Override
    public T getRenderer() {
        return renderer;
    }

    public void setRenderer(T renderer) {
        this.renderer = renderer;
    }

    @Override
    public Rectangle getBounds() {
        if (bounds != null) {
            return bounds;
        }

        if (renderer instanceof Bounded) {
            return ((Bounded) renderer).getBounds();
        }

        return Rectangle.EMPTY;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }
}
