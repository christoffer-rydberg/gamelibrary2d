package com.gamelibrary2d.splitscreen;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.AbstractComposedGameObject;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.denotations.*;

public class SplitLayer<T extends GameObject> extends AbstractComposedGameObject<T>
        implements Clearable, PointerDownAware, PointerMoveAware, PointerUpAware, Updatable {

    private final Rectangle renderArea;
    private SplitLayout layout;

    public SplitLayer(SplitLayout layout, Rectangle renderArea) {
        this.layout = layout;
        this.renderArea = renderArea;
    }

    public T getTarget() {
        return getContent();
    }

    public void setTarget(T obj) {
        setContent(obj);
    }

    public void setLayout(SplitLayout layout) {
        this.layout = layout;
    }

    @Override
    public void update(float deltaTime) {
        layout.update(getTarget(), renderArea, deltaTime);
        T content = getContent();
        if (content instanceof Updatable) {
            ((Updatable) content).update(deltaTime);
        }
    }

    @Override
    public void render(float alpha) {
        layout.render(alpha);
    }

    @Override
    public void clear() {
        T content = getContent();
        if (content instanceof Clearable) {
            ((Clearable) content).clear();
        }
    }

    @Override
    public boolean isAutoClearing() {
        T content = getContent();
        if (content instanceof Clearable) {
            return ((Clearable) content).isAutoClearing();
        }
        return false;
    }

    @Override
    public boolean pointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {
        T content = getContent();
        if (content instanceof PointerDownAware) {
            return ((PointerDownAware) content).pointerDown(id, button, x, y, transformedX, transformedY);
        }
        return false;
    }

    @Override
    public boolean pointerMove(int id, float x, float y, float transformedX, float transformedY) {
        T content = getContent();
        if (content instanceof PointerMoveAware) {
            return ((PointerMoveAware) content).pointerMove(id, x, y, transformedX, transformedY);
        }
        return false;
    }

    @Override
    public void pointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {
        T content = getContent();
        if (content instanceof PointerUpAware) {
            ((PointerUpAware) content).pointerUp(id, button, x, y, transformedX, transformedY);
        }
    }
}