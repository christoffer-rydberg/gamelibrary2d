package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.KeyAndPointerState;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Bounded;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Slider extends AbstractGameObject implements PointerDownAware, PointerMoveAware, PointerUpAware {
    private final Point pointerPosition = new Point();
    private final List<DragBeginListener> dragBeginListeners = new CopyOnWriteArrayList<>();
    private final List<DragStopListener> dragStopListeners = new CopyOnWriteArrayList<>();
    private final List<ValueChangedListener> valueChangedListeners = new CopyOnWriteArrayList<>();
    private final Handle handle;
    private final SliderDirection direction;
    private final float min;
    private final float max;
    private final float step;

    private float dragOriginX;
    private float dragOriginY;
    private Rectangle bounds;

    public Slider(Renderable handle, SliderDirection direction, float min, float max, float step) {
        this.handle = new Handle(handle);
        this.direction = direction;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public void addDragBeginListener(DragBeginListener listener) {
        dragBeginListeners.add(listener);
    }

    public void addDragStopListener(DragStopListener listener) {
        dragStopListeners.add(listener);
    }

    public void addValueChangedListener(ValueChangedListener listener) {
        valueChangedListeners.add(listener);
    }

    private int getValue() {
        return (int) ((direction == SliderDirection.HORIZONTAL
                ? handle.getPosition().getX()
                : handle.getPosition().getY()) / step);
    }

    public int setValue(int value, boolean publishEvent) {
        int actual = (int) Math.max(min, Math.min(max, value));
        switch (direction) {
            case HORIZONTAL:
                handle.setPosition(actual * step, 0);
                break;
            case VERTICAL:
                handle.setPosition(0, actual * step);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }

        if (publishEvent) {
            for (ValueChangedListener listener : valueChangedListeners) {
                listener.onValueChanged(actual);
            }
        }

        return actual;
    }

    public int setValue(int value) {
        return setValue(value, true);
    }

    private int getValueFromPosition(float x, float y) {
        switch (direction) {
            case HORIZONTAL:
                return (int) ((handle.getPosition().getX() + x - dragOriginX) / step);
            case VERTICAL:
                return (int) ((handle.getPosition().getY() + y - dragOriginY) / step);
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
    }

    @Override
    public boolean pointerDown(KeyAndPointerState state, int id, int button, float x, float y) {
        pointerPosition.set(x, y, this);
        return handle.pointerDown(state, id, button, pointerPosition.getX(), pointerPosition.getY());
    }

    @Override
    public boolean pointerMove(KeyAndPointerState state, int id, float x, float y) {
        pointerPosition.set(x, y, this);
        pointerPosition.transformTo(handle);

        if (id == handle.pointerId) {
            setValue(getValueFromPosition(pointerPosition.getX(), pointerPosition.getY()));
            return true;
        }

        return false;
    }

    @Override
    public void swallowedPointerMove(KeyAndPointerState state, int id) {

    }

    @Override
    public void pointerUp(KeyAndPointerState state, int id, int button, float x, float y) {
        pointerPosition.set(x, y, this);
        handle.pointerUp(state, id, button, pointerPosition.getX(), pointerPosition.getY());
    }

    @Override
    public Rectangle getBounds() {
        return bounds != null ? bounds : handle.getBounds();
    }

    protected void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    @Override
    protected void onRender(float alpha) {
        handle.render(alpha);
    }

    public enum SliderDirection {
        HORIZONTAL,
        VERTICAL
    }

    public interface DragBeginListener {
        void onDragBegin(int value);
    }

    public interface DragStopListener {
        void onDragStop(int value);
    }

    public interface ValueChangedListener {
        void onValueChanged(int value);
    }

    private class Handle extends AbstractGameObject implements PointerDownAware, PointerUpAware {
        private final Renderable renderer;
        private final Point pointerPosition = new Point();
        private int pointerId = -1;
        private int pointerButton = -1;

        public Handle(Renderable renderer) {
            this.renderer = renderer;
        }

        @Override
        public Rectangle getBounds() {
            if (renderer instanceof Bounded) {
                return ((Bounded) renderer).getBounds();
            }

            return Rectangle.EMPTY;
        }

        @Override
        protected void onRender(float alpha) {
            this.renderer.render(alpha);
        }

        @Override
        public boolean pointerDown(KeyAndPointerState state, int id, int button, float x, float y) {
            pointerPosition.set(x, y, this);
            if (getBounds().contains(pointerPosition)) {
                if (pointerId < 0) {
                    pointerId = id;
                    pointerButton = button;
                    dragOriginX = x;
                    dragOriginY = y;
                    for (DragBeginListener listener : dragBeginListeners) {
                        listener.onDragBegin(getValue());
                    }

                    return true;
                }
            }

            return false;
        }

        @Override
        public void pointerUp(KeyAndPointerState state, int id, int button, float x, float y) {
            if (pointerId == id && pointerButton == button) {
                pointerId = -1;
                pointerButton = -1;
                for (DragStopListener listener : dragStopListeners) {
                    listener.onDragStop(getValue());
                }
            }
        }
    }
}
