package com.gamelibrary2d.components.containers;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.denotations.Bounded;

import java.util.Comparator;
import java.util.List;

public abstract class AbstractLayerGameObject<T extends Renderable> extends AbstractGameObject implements LayerGameObject<T> {
    private final Point transformationPoint = new Point();

    private Rectangle bounds;
    private boolean updatesEnabled = true;

    @Override
    public int indexOf(Object obj) {
        return getLayer().indexOf(obj);
    }

    @Override
    public Comparator<T> getRenderOrderComparator() {
        return getLayer().getRenderOrderComparator();
    }

    @Override
    public void setRenderOrderComparator(Comparator<T> renderOrderComparator) {
        getLayer().setRenderOrderComparator(renderOrderComparator);
    }

    @Override
    public boolean isAutoClearing() {
        return getLayer().isAutoClearing();
    }

    public void setAutoClearing(boolean autoClearing) {
        getLayer().setAutoClearing(autoClearing);
    }

    @Override
    public void clear() {
        getLayer().clear();
    }

    @Override
    public T get(int index) {
        return getLayer().get(index);
    }

    @Override
    public void add(T obj) {
        getLayer().add(obj);
    }

    @Override
    public void add(int index, T obj) {
        getLayer().add(index, obj);
    }

    @Override
    public void remove(int index) {
        getLayer().remove(index);
    }

    @Override
    public boolean remove(Object obj) {
        return getLayer().remove(obj);
    }

    @Override
    public List<T> getItems() {
        return getLayer().getItems();
    }

    @Override
    public final boolean pointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {
        return isEnabled() && onPointerDown(id, button, x, y, transformedX, transformedY);
    }

    protected boolean onPointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {
        transformationPoint.set(transformedX, transformedY);
        transformationPoint.transformTo(this);
        return getLayer().pointerDown(id, button, x, y, transformationPoint.getX(), transformationPoint.getY());
    }

    @Override
    public final boolean pointerMove(int id, float x, float y, float transformedX, float transformedY) {
        return isEnabled() && onPointerMove(id, x, y, transformedX, transformedY);
    }

    protected boolean onPointerMove(int id, float x, float y, float transformedX, float transformedY) {
        transformationPoint.set(transformedX, transformedY);
        transformationPoint.transformTo(this);
        return getLayer().pointerMove(id, x, y, transformationPoint.getX(), transformationPoint.getY());
    }

    @Override
    public final void pointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {
        if (isEnabled()) {
            onPointerUp(id, button, x, y, transformedX, transformedY);
        }
    }

    protected void onPointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {
        transformationPoint.set(transformedX, transformedY);
        transformationPoint.transformTo(this);
        getLayer().pointerUp(id, button, x, y, transformationPoint.getX(), transformationPoint.getY());
    }

    @Override
    public final void update(float deltaTime) {
        if (isEnabled() && isUpdatesEnabled()) {
            onUpdate(deltaTime);
        }
    }

    protected void onUpdate(float deltaTime) {
        getLayer().update(deltaTime);
    }

    @Override
    public Rectangle getBounds() {
        return bounds != null ? bounds : getLayerBounds();
    }

    protected void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    private Rectangle getLayerBounds() {
        Layer<T> layer = getLayer();
        if (layer instanceof Bounded)
            return ((Bounded) layer).getBounds();
        else
            return Rectangle.EMPTY;
    }

    @Override
    public boolean isUpdatesEnabled() {
        return updatesEnabled;
    }

    @Override
    public void setUpdatesEnabled(boolean updatesEnabled) {
        this.updatesEnabled = updatesEnabled;
    }

    @Override
    public void onRender(float alpha) {
        getLayer().render(alpha);
    }

    protected abstract Layer<T> getLayer();
}
