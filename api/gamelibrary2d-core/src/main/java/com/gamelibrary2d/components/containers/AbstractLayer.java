package com.gamelibrary2d.components.containers;

import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;
import com.gamelibrary2d.denotations.Clearable;
import com.gamelibrary2d.denotations.Updatable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractLayer<T extends Renderable> implements Layer<T> {
    private final ArrayList<T> objects = new ArrayList<>();
    private final List<T> readonlyObjects = Collections.unmodifiableList(objects);
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
        objects.add(obj);
    }

    @Override
    public void add(int index, T obj) {
        objects.add(index, obj);
    }

    @Override
    public void remove(int index) {
        objects.remove(index);
    }

    @Override
    public boolean remove(Object obj) {
        return objects.remove(obj);
    }

    @Override
    public void clear() {
        try {
            prepareIteration(objects, Clearable.class);
            objects.clear();
            for (Object obj : iterationList) {
                if (((Clearable) obj).isAutoClearing()) {
                    clear((Clearable) obj);
                }
            }
        } finally {
            iterationList.clear();
        }
    }

    protected void clear(Clearable clearable) {
        clearable.clear();
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
    public List<T> getItems() {
        return readonlyObjects;
    }

    @Override
    public final boolean pointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {
        return isEnabled() && onPointerDown(id, button, x, y, transformedX, transformedY);
    }

    @Override
    public final boolean pointerMove(int id, float x, float y, float transformedX, float transformedY) {
        return isEnabled() && onPointerMove(id, x, y, transformedX, transformedY);
    }

    @Override
    public final void swallowedPointerMove(int id) {
        if (isEnabled()) {
            onSwallowedPointerMove(id);
        }
    }

    @Override
    public final void pointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {
        if (isEnabled()) {
            onPointerUp(id, button, x, y, transformedX, transformedY);
        }
    }

    protected boolean onPointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {
        try {
            prepareReverseIteration(objects, PointerDownAware.class);
            for (int i = 0; i < iterationList.size(); ++i) {
                PointerDownAware obj = (PointerDownAware) iterationList.get(i);
                if (onPointerDown(obj, id, button, x, y, transformedX, transformedY)) {
                    return true;
                }
            }
            return false;
        } finally {
            iterationList.clear();
        }
    }

    protected boolean onPointerDown(PointerDownAware obj, int id, int button, float x, float y, float transformedX, float transformedY) {
        return obj.pointerDown(id, button, x, y, transformedX, transformedY);
    }

    protected boolean onPointerMove(int id, float x, float y, float transformedX, float transformedY) {
        boolean swallowed = false;

        try {
            prepareReverseIteration(objects, PointerMoveAware.class);
            for (int i = 0; i < iterationList.size(); ++i) {
                PointerMoveAware obj = (PointerMoveAware) iterationList.get(i);
                if (swallowed) {
                    onSwallowedPointerMove(obj, id);
                } else {
                    swallowed = onPointerMove(obj, id, x, y, transformedX, transformedY);
                }
            }

            return swallowed;
        } finally {
            iterationList.clear();
        }
    }

    protected boolean onPointerMove(PointerMoveAware obj, int id, float x, float y, float transformedX, float transformedY) {
        return obj.pointerMove(id, x, y, transformedX, transformedY);
    }

    protected void onSwallowedPointerMove(int id) {
        try {
            prepareReverseIteration(objects, PointerMoveAware.class);
            for (int i = 0; i < iterationList.size(); ++i) {
                PointerMoveAware obj = (PointerMoveAware) iterationList.get(i);
                onSwallowedPointerMove(obj, id);
            }
        } finally {
            iterationList.clear();
        }
    }

    protected void onSwallowedPointerMove(PointerMoveAware obj, int id) {
        obj.swallowedPointerMove(id);
    }

    protected void onPointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {
        try {
            prepareReverseIteration(objects, PointerUpAware.class);
            for (int i = 0; i < iterationList.size(); ++i) {
                PointerUpAware obj = (PointerUpAware) iterationList.get(i);
                onPointerUp(obj, id, button, x, y, transformedX, transformedY);
            }
        } finally {
            iterationList.clear();
        }
    }

    protected void onPointerUp(PointerUpAware obj, int id, int button, float x, float y, float transformedX, float transformedY) {
        obj.pointerUp(id, button, x, y, transformedX, transformedY);
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
            onRender(objects.get(i), alpha);
        }
    }

    protected void onRender(T item, float alpha) {
        item.render(alpha);
    }

    @Override
    public final void update(float deltaTime) {
        if (isEnabled() && isUpdatesEnabled()) {
            handleUpdate(deltaTime);
        }
    }

    protected void handleUpdate(float deltaTime) {
        try {
            prepareReverseIteration(objects, Updatable.class);
            for (int i = 0; i < iterationList.size(); ++i) {
                onUpdate((Updatable) iterationList.get(i), deltaTime);
            }
        } finally {
            iterationList.clear();
        }
    }

    protected void onUpdate(Updatable item, float deltaTime) {
        item.update(deltaTime);
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

    private void prepareIteration(ArrayList<T> objects, Class<?> type) {
        for (int i = 0; i < objects.size(); ++i) {
            T obj = objects.get(i);
            if (type.isAssignableFrom(obj.getClass())) {
                iterationList.add(obj);
            }
        }
    }

    private void prepareReverseIteration(ArrayList<T> objects, Class<?> type) {
        for (int i = objects.size() - 1; i >= 0; --i) {
            T obj = objects.get(i);
            if (type.isAssignableFrom(obj.getClass())) {
                iterationList.add(obj);
            }
        }
    }
}