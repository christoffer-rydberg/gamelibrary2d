package com.gamelibrary2d.components;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.components.denotations.PixelAware;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;

public abstract class AbstractPointerAwareGameObject
        extends AbstractGameObject
        implements PointerDownAware, PointerMoveAware, PointerUpAware, PixelAware {
    private final Point transformationPoint = new Point();
    private final PointerInteractionsArray pointerInteractions = new PointerInteractionsArray(10);

    protected AbstractPointerAwareGameObject() {

    }

    public void setEnabled(boolean enabled) {
        if (isEnabled() != enabled) {
            super.setEnabled(enabled);
            clearPointerState();
        }
    }

    /**
     * The object holds an internal state tracking pointer interactions to know which pointers are active.
     * For this to be accurate, all pointer events must be forwarded to the object from the parent containers.
     * This method can be used to clear the internal state.
     */
    public void clearPointerState() {
        pointerInteractions.clear();
    }

    @Override
    public final boolean pointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {
        if (isEnabled()) {
            transformationPoint.set(transformedX, transformedY);
            transformationPoint.transformTo(this);
            if (isPixelVisible(transformationPoint.getX(), transformationPoint.getY()) && pointerInteractions.setActive(id, id)) {
                return onPointerDown(id, button, x, y, transformationPoint.getX(), transformationPoint.getY());
            }

            pointerInteractions.setInactive(id, id);
        }

        return false;
    }

    @Override
    public final void pointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {
        if (isEnabled() && pointerInteractions.isActive(id, id)) {
            pointerInteractions.setInactive(id, id);
            transformationPoint.set(transformedX, transformedY);
            transformationPoint.transformTo(this);
            onPointerUp(id, button, x, y, transformationPoint.getX(), transformationPoint.getY());
        }
    }

    @Override
    public final boolean pointerMove(int id, float x, float y, float transformedX, float transformedY) {
        if (isEnabled() && isTrackingPointerPositions()) {
            transformationPoint.set(transformedX, transformedY);
            transformationPoint.transformTo(this);
            if (isPixelVisible(transformationPoint.getX(), transformationPoint.getY())) {
                if (pointerInteractions.setOnTarget(id, true)) {
                    onPointerEntered(id);
                }

                return onPointerMove(id, x, y, transformationPoint.getX(), transformationPoint.getY());
            } else if (pointerInteractions.setOnTarget(id, false)) {
                onPointerLeft(id);
            }
        }

        return false;
    }

    @Override
    public final void swallowedPointerMove(int id) {
        if (isEnabled() && isTrackingPointerPositions()) {
            if (pointerInteractions.setOnTarget(id, false)) {
                onPointerLeft(id);
            }
        }
    }

    /**
     * Defaults to check if the pointer is within the object's {@link #getBounds bounds}.
     * Override to perform more granular hit detection.
     */
    @Override
    public boolean isPixelVisible(float x, float y) {
        return getBounds().contains(x, y);
    }

    /**
     * Invoked if the pointer is above the object's {@link #isPixelVisible visible area}.
     *
     * @param id           The id of the pointer.
     * @param button       The id of the pointer button.
     * @param x            The x-coordinate of the pointer.
     * @param y            The y-coordinate of the pointer.
     * @param transformedX The x-coordinate of the pointer transformed to the coordinate space represented by this object.
     * @param transformedY The y-coordinate of the pointer transformed to the coordinate space represented by this object.
     *
     * @return True if the event should be swallowed, false otherwise.
     */
    protected abstract boolean onPointerDown(int id, int button, float x, float y, float transformedX, float transformedY);

    /**
     * Invoked if the {@link #clearPointerState pointer state} contains the corresponding {@link #onPointerDown pointer down} event.
     * Note that this method is only invoked if the pointer event is routed by the parent container. If events are missed,
     * the object's {@link #clearPointerState pointer state} will not be correctly updated.
     *
     * @param id           The id of the pointer.
     * @param button       The id of the pointer button.
     * @param x            The x-coordinate of the pointer.
     * @param y            The y-coordinate of the pointer.
     * @param transformedX The x-coordinate of the pointer transformed to the coordinate space represented by this object.
     * @param transformedY The y-coordinate of the pointer transformed to the coordinate space represented by this object.
     */
    protected abstract void onPointerUp(int id, int button, float x, float y, float transformedX, float transformedY);

    /**
     * Must return true to enable {@link #onPointerEntered}, {@link #onPointerLeft} and {@link #onPointerMove}.
     * Should be disabled if the events are not needed for performance reasons, since the events are very frequent.
     */
    protected abstract boolean isTrackingPointerPositions();

    /**
     * Invoked if {@link #isTrackingPointerPositions tracking pointer positions} and the pointer enters the object's {@link #isPixelVisible visible area}.
     */
    protected abstract void onPointerEntered(int id);

    /**
     * Invoked if {@link #isTrackingPointerPositions tracking pointer positions} and the pointer leaves the object's {@link #isPixelVisible visible area}.
     */
    protected abstract void onPointerLeft(int id);

    /**
     * Invoked if {@link #isTrackingPointerPositions tracking pointer positions} and the pointer is above the object's {@link #isPixelVisible visible area}.
     *
     * @param id           The id of the pointer.
     * @param x            The x-coordinate of the pointer.
     * @param y            The y-coordinate of the pointer.
     * @param transformedX The x-coordinate of the pointer transformed to the coordinate space represented by this object.
     * @param transformedY The y-coordinate of the pointer transformed to the coordinate space represented by this object.
     *
     * @return True if the event should be swallowed, false otherwise.
     */
    protected abstract boolean onPointerMove(int id, float x, float y, float transformedX, float transformedY);

    private static class PointerInteractions {
        private int count;

        private boolean[] active;

        private int capacity = 0;

        private boolean onTarget;

        PointerInteractions(int initialArraySize) {
            this.active = new boolean[initialArraySize];
        }

        void clear() {
            for (int i = 0; i < capacity; ++i) {
                active[i] = false;
            }
            capacity = 0;
            onTarget = false;
        }

        boolean setOnTarget(boolean onTarget) {
            if(this.onTarget != onTarget) {
                this.onTarget = onTarget;
                return true;
            }

            return false;
        }

        void setActive(int button, boolean active) {
            capacity = Math.max(capacity, button + 1);

            if (button > this.active.length) {
                this.active = new boolean[capacity];
            }

            if (active != this.active[button]) {
                count += active ? 1 : -1;
                this.active[button] = active;
            }
        }

        boolean hasActiveButtons() {
            return count > 0;
        }

        boolean isActive(int button) {
            return button < active.length && active[button];
        }
    }

    private static class PointerInteractionsArray {
        private final PointerInteractions[] pointerInteractions;

        PointerInteractionsArray(int capacity) {
            pointerInteractions = new PointerInteractions[capacity];
        }

        boolean isActive(int id, int button) {
            PointerInteractions interactions = pointerInteractions[id];
            return interactions != null && interactions.isActive(button);
        }

        boolean setOnTarget(int id, boolean onTarget) {
            if (id > pointerInteractions.length) {
                return false;
            }

            PointerInteractions interactions = pointerInteractions[id];
            if (interactions == null) {
                interactions = new PointerInteractions(5);
                pointerInteractions[id] = interactions;
            }

            return interactions.setOnTarget(onTarget);
        }

        boolean setActive(int id, int button) {
            if (id > pointerInteractions.length) {
                return false;
            }

            PointerInteractions interactions = pointerInteractions[id];
            if (interactions == null) {
                interactions = new PointerInteractions(5);
                pointerInteractions[id] = interactions;
            }
            interactions.setActive(button, true);

            return true;
        }

        void setInactive(int id, int button) {
            PointerInteractions interactions = pointerInteractions[id];
            if (interactions != null) {
                interactions.setActive(button, false);
            }
        }

        void clear() {
            for (int i = 0; i < pointerInteractions.length; ++i) {
                PointerInteractions interactions = pointerInteractions[i];
                if (interactions != null) {
                    interactions.clear();
                }
            }
        }

        boolean hasActiveButtons(int id) {
            PointerInteractions interactions = pointerInteractions[id];
            return interactions != null && interactions.hasActiveButtons();
        }
    }
}