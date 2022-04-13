package com.gamelibrary2d.components;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.denotations.Bounded;
import com.gamelibrary2d.framework.Renderable;

public final class DefaultObservableGameObject<T extends Renderable> extends AbstractObservableGameObject<T> {
    private T content;
    private Rectangle bounds;

    public DefaultObservableGameObject() {

    }

    public DefaultObservableGameObject(T content) {
        this.content = content;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    @Override
    protected void onRender(float alpha) {
        content.render(alpha);
    }

    @Override
    public Rectangle getBounds() {
        if (bounds != null) {
            return bounds;
        }

        if (content instanceof Bounded) {
            return ((Bounded) content).getBounds();
        }

        return Rectangle.EMPTY;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }
}
