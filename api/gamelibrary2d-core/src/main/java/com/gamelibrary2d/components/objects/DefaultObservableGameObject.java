package com.gamelibrary2d.components.objects;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.denotations.Bounded;
import com.gamelibrary2d.framework.Renderable;

public final class DefaultObservableGameObject<T extends Renderable> extends AbstractObservableGameObject<T> {
    private Renderable background;
    private Renderable foreground;
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

    public Renderable getBackground() {
        return background;
    }

    public void setBackground(Renderable background) {
        this.background = background;
    }

    public Renderable getForeground() {
        return foreground;
    }

    public void setForeground(Renderable foreground) {
        this.foreground = foreground;
    }

    @Override
    protected void onRender(float alpha) {
        if (background != null) {
            background.render(alpha);
        }

        content.render(alpha);

        if (foreground != null) {
            foreground.render(alpha);
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
