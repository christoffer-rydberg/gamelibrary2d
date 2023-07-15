package com.gamelibrary2d.components.containers;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.PointerState;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.denotations.Bounded;

import java.util.Comparator;
import java.util.List;

public abstract class AbstractLayerGameObject<T extends Renderable> extends AbstractGameObject implements LayerGameObject<T> {
    private final Point pointerPosition = new Point();

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
    public final boolean pointerDown(PointerState pointerState, int id, int button, float transformedX, float transformedY) {
        return isEnabled() && onPointerDown(pointerState, id, button, transformedX, transformedY);
    }

    protected boolean onPointerDown(PointerState pointerState, int id, int button, float transformedX, float transformedY) {
        pointerPosition.set(transformedX, transformedY, this);
        return getLayer().pointerDown(pointerState, id, button, pointerPosition.getX(), pointerPosition.getY());
    }

    @Override
    public final boolean pointerMove(PointerState pointerState, int id, float transformedX, float transformedY) {
        return isEnabled() && onPointerMove(pointerState, id, transformedX, transformedY);
    }

    @Override
    public final void swallowedPointerMove(PointerState pointerState, int id) {
        if (isEnabled()) {
            onSwallowedPointerMove(pointerState, id);
        }
    }

    protected boolean onPointerMove(PointerState pointerState, int id, float transformedX, float transformedY) {
        pointerPosition.set(transformedX, transformedY, this);
        return getLayer().pointerMove(pointerState, id, pointerPosition.getX(), pointerPosition.getY());
    }

    protected void onSwallowedPointerMove(PointerState pointerState, int id) {
        getLayer().swallowedPointerMove(pointerState, id);
    }

    @Override
    public final void pointerUp(PointerState pointerState, int id, int button, float transformedX, float transformedY) {
        if (isEnabled()) {
            onPointerUp(pointerState, id, button, transformedX, transformedY);
        }
    }

    protected void onPointerUp(PointerState pointerState, int id, int button, float transformedX, float transformedY) {
        pointerPosition.set(transformedX, transformedY, this);
        getLayer().pointerUp(pointerState, id, button, pointerPosition.getX(), pointerPosition.getY());
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
