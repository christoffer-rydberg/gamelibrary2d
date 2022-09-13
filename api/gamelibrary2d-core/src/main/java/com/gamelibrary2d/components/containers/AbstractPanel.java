package com.gamelibrary2d.components.containers;

import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.GameObject;

import java.util.List;

public abstract class AbstractPanel<T extends GameObject>
        extends AbstractLayerGameObject<T>
        implements Panel<T> {

    private final Layer<T> layer = new DefaultLayer<>();
    private boolean autoResizing = true;

    protected AbstractPanel() {
        setAutoClearing(false);
    }

    @Override
    public boolean isAutoResizing() {
        return autoResizing;
    }

    @Override
    public void setAutoResizing(boolean autoResizing) {
        this.autoResizing = autoResizing;
    }

    @Override
    public void setBounds(Rectangle bounds) {
        super.setBounds(bounds);
    }

    @Override
    public void add(T obj) {
        onAdd(obj, true);
    }

    protected void onAdd(T obj, boolean increaseBounds) {
        super.add(obj);
        if (increaseBounds) {
            expandBounds(obj);
        }
    }

    @Override
    public void add(int index, T obj) {
        onAdd(index, obj, autoResizing);
    }

    protected void onAdd(int index, T obj, boolean increaseBounds) {
        super.add(index, obj);
        if (increaseBounds) {
            expandBounds(obj);
        }
    }

    @Override
    public void remove(int index) {
        onRemove(index, autoResizing);
    }

    protected void onRemove(int index, boolean recalculateBounds) {
        super.remove(index);
        if (recalculateBounds) {
            recalculateBounds();
        }
    }

    @Override
    public boolean remove(Object obj) {
        return onRemove(obj, true);
    }

    @Override
    protected Layer<T> getLayer() {
        return layer;
    }

    protected boolean onRemove(Object obj, boolean recalculateBounds) {
        if (super.remove(obj)) {
            if (recalculateBounds) {
                recalculateBounds();
            }
            return true;
        }

        return false;
    }

    @Override
    public void clear() {
        super.clear();
        if (autoResizing) {
            setBounds(Rectangle.EMPTY);
        }
    }

    /**
     * Expands the panel {@link #getBounds() bounds} to overlap the rotated and
     * scaled bounds of the specified object.
     */
    protected void expandBounds(T obj) {
        expandBounds(getExtentInPanel(obj));
    }

    /**
     * Expands the panel {@link #getBounds() bounds} to overlap the specified
     * bounds.
     */
    protected void expandBounds(Rectangle bounds) {
        Rectangle panelBounds = getBounds();

        if (panelBounds.isInfinite() || bounds.isEmpty()) {
            return;
        }

        if (panelBounds.isEmpty() || bounds.isInfinite()) {
            setBounds(bounds);
            return;
        }

        setBounds(panelBounds.add(bounds));
    }

    /**
     * Expands the panel {@link #getBounds() bounds} to overlap the specified point.
     */
    protected void expandBounds(Point point) {
        Rectangle panelBounds = getBounds();

        if (!panelBounds.isInfinite()) {
            setBounds(panelBounds.add(point));
        }
    }

    @Override
    public void recalculateBounds() {
        setBounds(Rectangle.EMPTY);
        List<T> objects = getChildren();
        for (int i = 0; i < objects.size(); ++i) {
            expandBounds(objects.get(i));
        }
    }

    @Override
    public void stack(T obj, StackOrientation orientation) {
        stack(obj, orientation, 0, false);
    }

    @Override
    public void stack(T obj, StackOrientation orientation, float offset) {
        stack(obj, orientation, offset, false);
    }

    @Override
    public void stack(T obj, StackOrientation orientation, float offset, boolean reposition) {
        if (getBounds().isInfinite()) {
            throw new IllegalStateException("Cannot stack object in panel with infinite bounds.");
        }

        stackObject(obj, orientation, offset);

        if (autoResizing) {
            Rectangle objectBounds = getExtentInPanel(obj);

            if (!objectBounds.equals(Rectangle.EMPTY)) {
                expandBounds(objectBounds);
            } else {
                expandBounds(obj.getPosition());
            }
        }

        if (!reposition) {
            onAdd(obj, false);
        }
    }

    /**
     * Calculates how much space the specified object will take up inside the panel.
     * This can be used to expand the bounds of the panel when the object is added.
     * The space is calculated from the position, rotation, scale and bounds of the
     * object. This method will return {@link Rectangle#EMPTY} if the panel's bounds
     * are infinite.
     */
    protected Rectangle getExtentInPanel(T obj) {
        if (getBounds().isInfinite()) {
            // Object cannot increase bounds any more.
            return Rectangle.EMPTY;
        }

        Rectangle objectBounds = obj.getBounds();
        if (objectBounds.isEmpty() || objectBounds.isInfinite()) {
            return objectBounds;
        }

        objectBounds = objectBounds.move(obj.getPosition());

        if (obj.getScale().getX() != 1 || obj.getScale().getY() != 1) {
            objectBounds = objectBounds.resize(obj.getScale(), obj.getPosition());
        }
        if (obj.getRotation() != 0) {
            objectBounds = objectBounds.rotate(obj.getRotation(), obj.getPosition());
        }
        return objectBounds;
    }

    private void stackObject(T obj, StackOrientation orientation, float offset) {
        float posX = obj.getPosition().getX();
        float posY = obj.getPosition().getY();

        obj.setPosition(0, 0);
        Rectangle objectBounds = getExtentInPanel(obj);
        if (objectBounds.isInfinite()) {
            obj.setPosition(posX, posY);
            throw new IllegalStateException("Stacked object must have valid bounds.");
        }

        switch (orientation) {
            case DOWN:
                posY = stackDown(objectBounds, offset);
                break;
            case LEFT:
                posX = stackLeft(objectBounds, offset);
                break;
            case RIGHT:
                posX = stackRight(objectBounds, offset);
                break;
            case UP:
                posY = stackUp(objectBounds, offset);
                break;
        }

        obj.setPosition(posX, posY);
    }

    private float stackLeft(Rectangle objectBounds, float margin) {
        return getBounds().getLowerX() - objectBounds.getUpperX() - margin;
    }

    private float stackUp(Rectangle objectBounds, float margin) {
        return getBounds().getUpperY() - objectBounds.getLowerY() + margin;
    }

    private float stackRight(Rectangle objectBounds, float margin) {
        return getBounds().getUpperX() - objectBounds.getLowerX() + margin;
    }

    private float stackDown(Rectangle objectBounds, float margin) {
        return getBounds().getLowerY() - objectBounds.getUpperY() - margin;
    }
}