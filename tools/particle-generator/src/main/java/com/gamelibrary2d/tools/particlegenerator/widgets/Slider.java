package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.DefaultObservableGameObject;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Slider extends AbstractGameObject implements PointerDownAware, PointerMoveAware, PointerUpAware {
    private final Point transformationPoint = new Point();
    private final List<DragBeginListener> dragBeginListeners = new CopyOnWriteArrayList<>();
    private final List<DragStopListener> dragStopListeners = new CopyOnWriteArrayList<>();
    private final List<ValueChangedListener> valueChangedListeners = new CopyOnWriteArrayList<>();
    private final DefaultObservableGameObject<Renderable> handle;
    private final SliderDirection direction;
    private final float min;
    private final float max;
    private final float step;
    private int pointerId = -1;
    private int pointerButton = -1;
    private float dragOriginX;
    private float dragOriginY;
    private Rectangle bounds;

    public Slider(Renderable handle, SliderDirection direction, float min, float max, float step) {
        this.handle = createHandle(handle);
        this.direction = direction;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public Slider(Renderable handle, SliderDirection direction) {
        this(handle, direction, 0, 100, 1);
    }

    public Slider(Renderable handle) {
        this(handle, SliderDirection.HORIZONTAL);
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

    private DefaultObservableGameObject<Renderable> createHandle(Renderable content) {
        DefaultObservableGameObject<Renderable> handleObj = new DefaultObservableGameObject<>(content);
        handleObj.addPointerDownListener(this::onHandleClicked);
        handleObj.addPointerDragListener(this::onHandleDragged);
        handleObj.addPointerUpListener(this::onHandleReleased);
        return handleObj;
    }

    @Override
    public boolean pointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {
        if (isEnabled()) {
            transformationPoint.set(transformedX, transformedY);
            transformationPoint.transformTo(this);
            return handle.pointerDown(id, button, x, y, transformationPoint.getX(), transformationPoint.getY());
        }

        return false;
    }

    @Override
    public boolean pointerMove(int id, float x, float y, float transformedX, float transformedY) {
        if (isEnabled()) {
            transformationPoint.set(transformedX, transformedY);
            transformationPoint.transformTo(this);
            return handle.pointerMove(id, x, y, transformationPoint.getX(), transformationPoint.getY());
        }

        return false;
    }

    @Override
    public void pointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {
        if (isEnabled()) {
            transformationPoint.set(transformedX, transformedY);
            transformationPoint.transformTo(this);
            handle.pointerUp(id, button, x, y, transformationPoint.getX(), transformationPoint.getY());
        }
    }

    private void onHandleClicked(int id, int button, float x, float y, float transformedX, float transformedY) {
        if (pointerId < 0) {
            pointerId = id;
            pointerButton = button;
            dragOriginX = transformedX;
            dragOriginY = transformedY;
            for (DragBeginListener listener : dragBeginListeners) {
                listener.onDragBegin(getValue());
            }
        }
    }

    private void onHandleReleased(int id, int button, float x, float y, float transformedX, float transformedY) {
        if (pointerId == id && pointerButton == button) {
            pointerId = -1;
            pointerButton = -1;
            for (DragStopListener listener : dragStopListeners) {
                listener.onDragStop(getValue());
            }
        }
    }

    private void onHandleDragged(int id, float x, float y, float transformedX, float transformedY) {
        if (pointerId == id) {
            setValue(getValueFromPosition(transformedX, transformedY));
        }
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
}
