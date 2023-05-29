package com.gamelibrary2d.components;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.denotations.Bounded;

public final class DefaultObservableGameObject<T extends Renderable> extends AbstractObservableGameObject {
    private T renderer;
    private Rectangle bounds;

    public DefaultObservableGameObject() {

    }

    public DefaultObservableGameObject(T renderer) {
        this.renderer = renderer;
    }

    public T getRenderer() {
        return renderer;
    }

    public void setRenderer(T renderer) {
        this.renderer = renderer;
    }

    @Override
    protected void onRender(float alpha) {
        if (renderer != null) {
            renderer.render(alpha);
        }
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
