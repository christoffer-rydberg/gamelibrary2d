package com.gamelibrary2d.components;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.denotations.Bounded;

public final class DefaultGameObject<T extends Renderable> extends AbstractGameObject {
    private T renderer;
    private Rectangle bounds;

    public DefaultGameObject() {

    }

    public DefaultGameObject(T renderer) {
        this.renderer = renderer;
    }

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

    @Override
    protected void onRender(float alpha) {
        if (renderer != null) {
            renderer.render(alpha);
        }
    }
}
