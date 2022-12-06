package com.gamelibrary2d.components;

import com.gamelibrary2d.FocusManager;
import com.gamelibrary2d.Point;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerDownWhenFocusedAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;
import com.gamelibrary2d.disposal.DefaultDisposer;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.opengl.renderers.FrameBufferRenderer;
import com.gamelibrary2d.opengl.resources.DefaultFrameBuffer;
import com.gamelibrary2d.opengl.resources.FrameBuffer;

public abstract class AbstractPointerAwareGameObject
        extends AbstractGameObject
        implements PointerDownAware, PointerMoveAware, PointerUpAware, PointerDownWhenFocusedAware {
    private final Point transformationPoint = new Point();
    private final PointerInteractionsArray pointerInteractions = new PointerInteractionsArray(10);
    private FrameBufferRenderer frameBufferRenderer;
    private DefaultDisposer frameBufferDisposer;

    protected AbstractPointerAwareGameObject() {

    }

    public void setEnabled(boolean enabled) {
        if (isEnabled() != enabled) {
            super.setEnabled(enabled);
            pointerInteractions.clear();
        }
    }

    public void disablePixelDetection() {
        if (this.frameBufferDisposer != null) {
            this.frameBufferDisposer.dispose();
            this.frameBufferDisposer = null;
            frameBufferRenderer = null;
        }
    }

    public void enablePixelDetection(Disposer disposer) {
        if (!pixelDetectionEnabled()) {
            this.frameBufferDisposer = new DefaultDisposer(disposer);
        } else if (disposer != this.frameBufferDisposer.getParent()) {
            disablePixelDetection();
            this.frameBufferDisposer = new DefaultDisposer(disposer);
        }
    }

    public boolean pixelDetectionEnabled() {
        return frameBufferDisposer != null;
    }

    private boolean isPixelVisible(float x, float y) {
        Rectangle bounds = getBounds();
        if (bounds.contains(x, y)) {
            if (pixelDetectionEnabled()) {
                if (frameBufferRenderer == null || !frameBufferRenderer.getArea().equals(bounds)) {
                    frameBufferDisposer.dispose();

                    FrameBuffer frameBuffer = DefaultFrameBuffer.create(
                            (int) bounds.getWidth(),
                            (int) bounds.getHeight(),
                            frameBufferDisposer);

                    frameBufferRenderer = new FrameBufferRenderer(bounds, frameBuffer);
                }

                Renderable renderer = getRenderer();
                if (renderer != null) {
                    frameBufferRenderer.render(renderer, 1f);
                    return frameBufferRenderer.isVisible(x, y);
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        return false;
    }

    @Override
    public final boolean pointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {
        if (isEnabled()) {
            transformationPoint.set(transformedX, transformedY);
            transformationPoint.transformTo(this);
            if (isPixelVisible(transformationPoint.getX(), transformationPoint.getY()) && pointerInteractions.setActive(id, id)) {
                onPointerDown(id, button, x, y, transformationPoint.getX(), transformationPoint.getY());
                return true;
            }

            pointerInteractions.setInactive(id, id);
        }

        return false;
    }

    @Override
    public final boolean pointerMove(int id, float x, float y, float transformedX, float transformedY) {
        if (isEnabled()) {
            if (pointerInteractions.hasActiveButtons(id) && isListeningToPointDragEvents()) {
                transformationPoint.set(transformedX, transformedY);
                transformationPoint.transformTo(this);
                onPointerDrag(id, x, y, transformationPoint.getX(), transformationPoint.getY());
                return true;
            } else if (isListeningToPointHoverEvents()) {
                transformationPoint.set(transformedX, transformedY);
                transformationPoint.transformTo(this);
                if (isPixelVisible(transformationPoint.getX(), transformationPoint.getY())) {
                    onPointerHover(id, x, y, transformationPoint.getX(), transformationPoint.getY());
                    return true;
                }
            }
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
    public void pointerDownWhenFocused(int id, int button) {
        if (isEnabled() && !pointerInteractions.isActive(id, id)) {
            FocusManager.unfocus(this, false);
        }
    }

    protected boolean isListeningToPointHoverEvents() {
        return false;
    }

    protected boolean isListeningToPointDragEvents() {
        return false;
    }

    /**
     * Override this method to handle pointer-down actions.
     *
     * @param id           The id of the pointer.
     * @param button       The id of the pointer button.
     * @param x            The x-coordinate of the pointer.
     * @param y            The y-coordinate of the pointer.
     * @param transformedX The x-coordinate of the pointer transformed to the coordinate space represented by this object.
     * @param transformedY The y-coordinate of the pointer transformed to the coordinate space represented by this object.
     */
    protected void onPointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {

    }

    /**
     * Override this method to handle pointer-up actions.
     *
     * @param id           The id of the pointer.
     * @param button       The id of the pointer button.
     * @param x            The x-coordinate of the pointer.
     * @param y            The y-coordinate of the pointer.
     * @param transformedX The x-coordinate of the pointer transformed to the coordinate space represented by this object.
     * @param transformedY The y-coordinate of the pointer transformed to the coordinate space represented by this object.
     */
    protected void onPointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {

    }

    /**
     * Override this method to handle pointer-hover actions. Note that {@link #isListeningToPointHoverEvents}
     * must be overridden as well to return true.
     *
     * @param id           The id of the pointer.
     * @param x            The x-coordinate of the pointer.
     * @param y            The y-coordinate of the pointer.
     * @param transformedX The x-coordinate of the pointer transformed to the coordinate space represented by this object.
     * @param transformedY The y-coordinate of the pointer transformed to the coordinate space represented by this object.
     */
    protected void onPointerHover(int id, float x, float y, float transformedX, float transformedY) {

    }

    /**
     * Override this method to handle pointer-drag actions. Note that {@link #isListeningToPointDragEvents}
     * must be overridden as well to return true.
     *
     * @param id           The id of the pointer.
     * @param x            The x-coordinate of the pointer.
     * @param y            The y-coordinate of the pointer.
     * @param transformedX The x-coordinate of the pointer transformed to the coordinate space represented by this object.
     * @param transformedY The y-coordinate of the pointer transformed to the coordinate space represented by this object.
     */
    protected void onPointerDrag(int id, float x, float y, float transformedX, float transformedY) {

    }

    private static class PointerInteractions {
        private int count;

        private boolean[] active;

        private int capacity = 0;

        PointerInteractions(int initialArraySize) {
            this.active = new boolean[initialArraySize];
        }

        void clear() {
            for (int i = 0; i < capacity; ++i) {
                active[i] = false;
            }
            capacity = 0;
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