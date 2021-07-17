package com.gamelibrary2d.components.containers;

import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.components.denotations.*;

import java.util.Comparator;

/**
 * Defines a layer of {@link Renderable} objects.
 */
public interface Layer<T extends Renderable> extends Parent<T>, Renderable, Clearable, Updatable, PointerAware, Opacifiable {

    T get(int index);

    int indexOf(Object obj);

    void add(T obj);

    void add(int index, T obj);

    void remove(int index);

    boolean remove(Object obj);

    void setAutoClearing(boolean autoClearing);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    /**
     * @return The comparator used to sort game objects before rendering.
     */
    Comparator<T> getRenderOrderComparator();

    /**
     * Sets the comparator used to sort items before rendering. If the
     * comparator is null (default) no sorting will be done and objects will be
     * rendered the order they are inserted. However, if a comparator has been set,
     * all objects will be reordered on the first render call. Setting the
     * comparator back to null will not restore the object order.
     *
     * @param renderOrderComparator The comparator used to sort items before rendering.
     */
    void setRenderOrderComparator(Comparator<T> renderOrderComparator);
}