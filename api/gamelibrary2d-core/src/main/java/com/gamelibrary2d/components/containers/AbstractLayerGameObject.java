package com.gamelibrary2d.components.containers;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.KeyAndPointerState;
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
    public boolean isEnabled() {
        return getLayer().isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        getLayer().setEnabled(enabled);
    }

    @Override
    public final boolean pointerDown(KeyAndPointerState keyAndPointerState, int id, int button, float x, float y) {
        return isEnabled() && onPointerDown(keyAndPointerState, id, button, x, y);
    }

    protected boolean onPointerDown(KeyAndPointerState keyAndPointerState, int id, int button, float x, float y) {
        pointerPosition.set(x, y, this);
        return getLayer().pointerDown(keyAndPointerState, id, button, pointerPosition.getX(), pointerPosition.getY());
    }

    @Override
    public final boolean pointerMove(KeyAndPointerState keyAndPointerState, int id, float x, float y) {
        return isEnabled() && onPointerMove(keyAndPointerState, id, x, y);
    }

    @Override
    public final void swallowedPointerMove(KeyAndPointerState keyAndPointerState, int id) {
        if (isEnabled()) {
            onSwallowedPointerMove(keyAndPointerState, id);
        }
    }

    protected boolean onPointerMove(KeyAndPointerState keyAndPointerState, int id, float x, float y) {
        pointerPosition.set(x, y, this);
        return getLayer().pointerMove(keyAndPointerState, id, pointerPosition.getX(), pointerPosition.getY());
    }

    protected void onSwallowedPointerMove(KeyAndPointerState keyAndPointerState, int id) {
        getLayer().swallowedPointerMove(keyAndPointerState, id);
    }

    @Override
    public final void pointerUp(KeyAndPointerState keyAndPointerState, int id, int button, float x, float y) {
        if (isEnabled()) {
            onPointerUp(keyAndPointerState, id, button, x, y);
        }
    }

    protected void onPointerUp(KeyAndPointerState keyAndPointerState, int id, int button, float x, float y) {
        pointerPosition.set(x, y, this);
        getLayer().pointerUp(keyAndPointerState, id, button, pointerPosition.getX(), pointerPosition.getY());
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
