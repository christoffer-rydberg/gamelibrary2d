package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.PointerState;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;
import com.gamelibrary2d.demos.networkgame.client.input.Controller;
import com.gamelibrary2d.demos.networkgame.client.input.ControllerInputId;
import com.gamelibrary2d.demos.networkgame.client.objects.network.LocalPlayer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AccelerationArea implements Renderable, PointerDownAware, PointerMoveAware, PointerUpAware {
    private final Rectangle bounds;
    private final LocalPlayer player;
    private final List<ValueChangedListener> valueChangedListeners = new CopyOnWriteArrayList<>();

    private int pointerId = -1;
    private int pointerButton = -1;
    private float value;

    private AccelerationArea(Rectangle bounds, LocalPlayer player) {
        this.bounds = bounds;
        this.player = player;
    }

    public static AccelerationArea create(Rectangle bounds, LocalPlayer player) {
        return new AccelerationArea(bounds, player);
    }

    public void addValueChangedListener(ValueChangedListener listener) {
        valueChangedListeners.add(listener);
    }

    public void removeValueChangedListener(ValueChangedListener listener) {
        valueChangedListeners.remove(listener);
    }

    private void setValue(float value) {
        float controllerValue = Math.min(value, 1f);
        if (this.value != controllerValue) {
            this.value = controllerValue;

            Controller controller = player.getController();
            controller.setValue(ControllerInputId.UP, controllerValue);

            for (ValueChangedListener listener : valueChangedListeners) {
                listener.onValueChanged(controllerValue);
            }
        }
    }

    @Override
    public void render(float alpha) {

    }

    @Override
    public boolean pointerDown(PointerState pointerState,  int id, int button, float x, float y) {
        if (bounds.contains(x, y)) {
            if (pointerId < 0) {
                pointerId = id;
                pointerButton = button;
                setValue(1f);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean pointerMove(PointerState pointerState, int id, float x, float y) {
        return pointerId == id;
    }

    @Override
    public void swallowedPointerMove(PointerState pointerState, int id) {

    }

    @Override
    public void pointerUp(PointerState pointerState, int id, int button, float x, float y) {
        if (pointerId == id && pointerButton == button) {
            pointerId = -1;
            pointerButton = -1;
            setValue(0f);
        }
    }

    public interface ValueChangedListener {
        void onValueChanged(float value);
    }
}
