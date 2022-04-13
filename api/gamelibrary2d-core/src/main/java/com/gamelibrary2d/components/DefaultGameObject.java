package com.gamelibrary2d.components;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.denotations.Bounded;
import com.gamelibrary2d.framework.Renderable;

public final class DefaultGameObject<T extends Renderable> extends AbstractGameObject {
    private T content;
    private Rectangle bounds;

    public DefaultGameObject() {

    }

    public DefaultGameObject(T content) {
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
        if (content != null) {
            content.render(alpha);
        }
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
