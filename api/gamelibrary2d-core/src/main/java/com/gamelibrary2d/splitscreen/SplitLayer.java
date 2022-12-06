package com.gamelibrary2d.splitscreen;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.AbstractPointerAwareComposedGameObject;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;
import com.gamelibrary2d.denotations.Clearable;
import com.gamelibrary2d.denotations.Updatable;

public class SplitLayer<T extends GameObject> extends AbstractPointerAwareComposedGameObject<T>
        implements Clearable, PointerDownAware, PointerMoveAware, PointerUpAware, Updatable {

    private final Rectangle renderArea;
    private SplitLayout layout;

    private T target;

    public SplitLayer(SplitLayout layout, Rectangle renderArea) {
        this.layout = layout;
        this.renderArea = renderArea;
    }

    @Override
    protected T getComposition() {
        return target;
    }

    public T getTarget() {
        return target;
    }

    public void setTarget(T target) {
        this.target = target;
    }

    public void setLayout(SplitLayout layout) {
        this.layout = layout;
    }

    @Override
    public void update(float deltaTime) {
        layout.update(getTarget(), renderArea, deltaTime);
        if (target instanceof Updatable) {
            ((Updatable) target).update(deltaTime);
        }
    }

    @Override
    public void render(float alpha) {
        layout.render(alpha);
    }

    @Override
    public void clear() {
        if (target instanceof Clearable) {
            ((Clearable) target).clear();
        }
    }

    @Override
    public boolean isAutoClearing() {
        if (target instanceof Clearable) {
            return ((Clearable) target).isAutoClearing();
        }
        return false;
    }
}