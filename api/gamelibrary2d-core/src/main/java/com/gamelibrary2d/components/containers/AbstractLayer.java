package com.gamelibrary2d.components.containers;

import com.gamelibrary2d.PointerState;
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
    private final List<Object> reusableIterationList = new ArrayList<>();
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
        List<Object> iterationList = prepareIteration(objects);
        try {
            objects.clear();
            for (Object obj : iterationList) {
                if (obj instanceof Clearable) {
                    Clearable clearable = (Clearable) obj;
                    if (clearable.isAutoClearing()) {
                        clear(clearable);
                    }
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
    public final boolean pointerDown(PointerState pointerState, int id, int button, float x, float y) {
        return isEnabled() && onPointerDown(pointerState, id, button, x, y);
    }

    @Override
    public final boolean pointerMove(PointerState pointerState, int id, float x, float y) {
        return isEnabled() && onPointerMove(pointerState, id, x, y);
    }

    @Override
    public final void swallowedPointerMove(PointerState pointerState, int id) {
        if (isEnabled()) {
            onSwallowedPointerMove(pointerState, id);
        }
    }

    @Override
    public final void pointerUp(PointerState pointerState, int id, int button, float x, float y) {
        if (isEnabled()) {
            onPointerUp(pointerState, id, button, x, y);
        }
    }

    protected boolean onPointerDown(PointerState pointerState, int id, int button, float x, float y) {
        List<Object> iterationList = prepareReverseIteration(objects);
        try {
            for (int i = 0; i < iterationList.size(); ++i) {
                if (onPointerDown((T) iterationList.get(i), pointerState, id, button, x, y)) {
                    return true;
                }
            }
            return false;
        } finally {
            iterationList.clear();
        }
    }

    protected boolean onPointerDown(T obj, PointerState pointerState, int id, int button, float x, float y) {
        if (obj instanceof PointerDownAware) {
            return ((PointerDownAware) obj).pointerDown(pointerState, id, button, x, y);
        }

        return false;
    }

    protected boolean onPointerMove(PointerState pointerState, int id, float x, float y) {
        boolean swallowed = false;

        List<Object> iterationList = prepareReverseIteration(objects);
        try {
            for (int i = 0; i < iterationList.size(); ++i) {
                if (swallowed) {
                    onSwallowedPointerMove((T) iterationList.get(i), pointerState, id);
                } else {
                    swallowed = onPointerMove((T) iterationList.get(i), pointerState, id, x, y);
                }
            }

            return swallowed;
        } finally {
            iterationList.clear();
        }
    }

    protected boolean onPointerMove(T obj, PointerState pointerState, int id, float x, float y) {
        if (obj instanceof PointerMoveAware) {
            return ((PointerMoveAware) obj).pointerMove(pointerState, id, x, y);
        }

        return false;
    }

    protected void onSwallowedPointerMove(PointerState pointerState, int id) {
        List<Object> iterationList = prepareReverseIteration(objects);
        try {
            for (int i = 0; i < iterationList.size(); ++i) {
                onSwallowedPointerMove((T) iterationList.get(i), pointerState, id);
            }
        } finally {
            iterationList.clear();
        }
    }

    protected void onSwallowedPointerMove(T obj, PointerState pointerState, int id) {
        if (obj instanceof PointerMoveAware) {
            ((PointerMoveAware) obj).swallowedPointerMove(pointerState, id);
        }
    }

    protected void onPointerUp(PointerState pointerState, int id, int button, float x, float y) {
        List<Object> iterationList = prepareReverseIteration(objects);
        try {
            for (int i = 0; i < iterationList.size(); ++i) {
                onPointerUp((T) iterationList.get(i), pointerState, id, button, x, y);
            }
        } finally {
            iterationList.clear();
        }
    }

    protected void onPointerUp(T obj, PointerState pointerState, int id, int button, float x, float y) {
        if (obj instanceof PointerUpAware) {
            ((PointerUpAware) obj).pointerUp(pointerState,id, button, x, y);
        }
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
        List<Object> iterationList = prepareIteration(objects);
        try {
            for (int i = 0; i < iterationList.size(); ++i) {
                Object obj = iterationList.get(i);
                if (obj instanceof Updatable) {
                    onUpdate((Updatable) iterationList.get(i), deltaTime);
                }
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

    private List<Object> prepareIteration(ArrayList<T> objects) {
        List<Object> iterationList = reusableIterationList.isEmpty() ? reusableIterationList : new ArrayList<>();
        for (int i = 0; i < objects.size(); ++i) {
            iterationList.add(objects.get(i));
        }

        return iterationList;
    }

    private List<Object> prepareReverseIteration(ArrayList<T> objects) {
        List<Object> iterationList = reusableIterationList.isEmpty() ? reusableIterationList : new ArrayList<>();
        for (int i = objects.size() - 1; i >= 0; --i) {
            iterationList.add(objects.get(i));
        }

        return iterationList;
    }
}