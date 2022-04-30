package com.gamelibrary2d.components.containers;

import com.gamelibrary2d.components.denotations.*;
import com.gamelibrary2d.framework.Renderable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractLayer<T extends Renderable> implements Layer<T> {
    private final List<T> objects = new ArrayList<>();
    private final List<T> readonlyObjects = Collections.unmodifiableList(objects);
    private final List<Updatable> updatableObjects = new ArrayList<>();
    private final List<Clearable> clearableObjects = new ArrayList<>();
    private final List<PointerDownAware> pointerDownAwareObjects = new ArrayList<>();
    private final List<PointerMoveAware> pointerMoveAwareObjects = new ArrayList<>();
    private final List<PointerUpAware> pointerUpAwareObjects = new ArrayList<>();

    private final List<Object> iterationList = new ArrayList<>();

    private Comparator<T> renderOrderComparator;

    private boolean autoClearing = true;
    private boolean enabled = true;
    private boolean updatesEnabled = true;
    private float opacity = 1f;

    @Override
    public int indexOf(Object obj) {
        return objects.indexOf(obj);
    }

    @Override
    public Comparator<T> getRenderOrderComparator() {
        return renderOrderComparator;
    }

    @Override
    public void setRenderOrderComparator(Comparator<T> renderOrderComparator) {
        this.renderOrderComparator = renderOrderComparator;
    }

    @Override
    public T get(int index) {
        return objects.get(index);
    }

    @Override
    public void add(T obj) {
        addType(obj);
        objects.add(obj);
    }

    @Override
    public void add(int index, T obj) {
        addType(obj);
        objects.add(index, obj);
    }

    private void addType(T obj) {
        if (obj instanceof Updatable)
            updatableObjects.add((Updatable) obj);
        if (obj instanceof Clearable)
            clearableObjects.add((Clearable) obj);
        if (obj instanceof PointerDownAware)
            pointerDownAwareObjects.add((PointerDownAware) obj);
        if (obj instanceof PointerMoveAware)
            pointerMoveAwareObjects.add((PointerMoveAware) obj);
        if (obj instanceof PointerUpAware)
            pointerUpAwareObjects.add((PointerUpAware) obj);
    }

    @Override
    public void remove(int index) {
        removeType(objects.get(index));
        objects.remove(index);
    }

    @Override
    public boolean remove(Object obj) {
        if (objects.remove(obj)) {
            removeType(obj);
            return true;
        }
        return false;
    }

    private void removeType(Object obj) {
        if (obj instanceof Updatable)
            updatableObjects.remove(obj);
        if (obj instanceof Clearable)
            clearableObjects.remove(obj);
        if (obj instanceof PointerDownAware)
            pointerDownAwareObjects.remove(obj);
        if (obj instanceof PointerMoveAware)
            pointerMoveAwareObjects.remove(obj);
        if (obj instanceof PointerUpAware)
            pointerUpAwareObjects.remove(obj);
    }

    @Override
    public void clear() {
        objects.clear();
        updatableObjects.clear();
        pointerDownAwareObjects.clear();
        pointerMoveAwareObjects.clear();
        pointerUpAwareObjects.clear();
        for (Clearable clearable : clearableObjects) {
            clear(clearable);
        }
        clearableObjects.clear();
    }

    private void clear(Clearable clearable) {
        if (clearable.isAutoClearing()) {
            clearable.clear();
        }
    }

    @Override
    public boolean isAutoClearing() {
        return autoClearing;
    }

    @Override
    public void setAutoClearing(boolean autoClearing) {
        this.autoClearing = autoClearing;
    }

    @Override
    public List<T> getChildren() {
        return readonlyObjects;
    }

    @Override
    public final boolean pointerDown(int id, int button, float x, float y, float projectedX, float projectedY) {
        return isEnabled() ? onPointerDown(id, button, x, y, projectedX, projectedY) : false;
    }

    @Override
    public final boolean pointerMove(int id, float x, float y, float projectedX, float projectedY) {
        return isEnabled() ? onPointerMove(id, x, y, projectedX, projectedY) : false;
    }

    @Override
    public final void pointerUp(int id, int button, float x, float y, float projectedX, float projectedY) {
        if (isEnabled()) {
            onPointerUp(id, button, x, y, projectedX, projectedY);
        }
    }

    protected boolean onPointerDown(int id, int button, float x, float y, float projectedX, float projectedY) {
        iterationList.addAll(pointerDownAwareObjects);
        for (int i = iterationList.size() - 1; i >= 0; --i) {
            PointerDownAware obj = (PointerDownAware) iterationList.get(i);
            if (obj.pointerDown(id, button, x, y, projectedX, projectedY)) {
                iterationList.clear();
                return true;
            }
        }

        iterationList.clear();
        return false;
    }

    protected boolean onPointerMove(int id, float x, float y, float projectedX, float projectedY) {
        iterationList.addAll(pointerMoveAwareObjects);
        for (int i = iterationList.size() - 1; i >= 0; --i) {
            PointerMoveAware obj = (PointerMoveAware) iterationList.get(i);
            if (obj.pointerMove(id, x, y, projectedX, projectedY)) {
                iterationList.clear();
                return true;
            }
        }

        iterationList.clear();
        return false;
    }

    protected void onPointerUp(int id, int button, float x, float y, float projectedX, float projectedY) {
        iterationList.addAll(pointerUpAwareObjects);
        for (int i = iterationList.size() - 1; i >= 0; --i) {
            PointerUpAware obj = (PointerUpAware) iterationList.get(i);
            obj.pointerUp(id, button, x, y, projectedX, projectedY);
        }

        iterationList.clear();
    }

    @Override
    public final void render(float alpha) {
        if (isEnabled()) {
            onRender(alpha * opacity);
        }
    }

    protected void onRender(float alpha) {
        if (renderOrderComparator != null) {
            objects.sort(renderOrderComparator);
        }

        int size = objects.size();
        for (int i = 0; i < size; ++i) {
            objects.get(i).render(alpha);
        }
    }

    @Override
    public final void update(float deltaTime) {
        if (isEnabled() && isUpdatesEnabled()) {
            onUpdate(deltaTime);
        }
    }

    protected void onUpdate(float deltaTime) {
        int size = updatableObjects.size();
        for (int i = 0; i < size; ++i) {
            updatableObjects.get(i).update(deltaTime);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
    public float getOpacity() {
        return opacity;
    }

    @Override
    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }
}