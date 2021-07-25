package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;
import com.gamelibrary2d.components.denotations.Updatable;
import com.gamelibrary2d.demos.networkgame.client.input.Controller;
import com.gamelibrary2d.demos.networkgame.client.input.ControllerInputId;
import com.gamelibrary2d.demos.networkgame.client.objects.network.LocalPlayer;
import com.gamelibrary2d.framework.Renderable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AccelerationArea implements Renderable, Updatable, PointerDownAware, PointerMoveAware, PointerUpAware {
    private final Point origin = new Point();
    private final Rectangle bounds;
    private final LocalPlayer player;
    private final float max, step;
    private final List<ValueChangedListener> valueChangedListeners = new CopyOnWriteArrayList<>();

    private int pointerId = -1;
    private int pointerButton = -1;
    private float acceleration;
    private float value;

    private AccelerationArea(Rectangle bounds, LocalPlayer player, float maxDistance) {
        this.bounds = bounds;
        this.player = player;
        this.max = maxDistance;
        this.step = 1f;
    }

    public static AccelerationArea create(Rectangle bounds, LocalPlayer player, float maxDistance) {
        return new AccelerationArea(
                bounds,
                player,
                maxDistance);
    }

    public void addValueChangedListener(ValueChangedListener listener) {
        valueChangedListeners.add(listener);
    }

    public void removeValueChangedListener(ValueChangedListener listener) {
        valueChangedListeners.remove(listener);
    }

    private void setValue(float value) {
        float controllerValue = Math.max(0f, Math.min(max, value)) / max;
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
    public boolean pointerDown(int id, int button, float x, float y, float projectedX, float projectedY) {
        if (bounds.contains(projectedX, projectedY)) {
            if (pointerId < 0) {
                pointerId = id;
                pointerButton = button;
                origin.set(projectedX, projectedY);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean pointerMove(int id, float x, float y, float projectedX, float projectedY) {
        if (pointerId == id) {
            acceleration = (projectedY - origin.getY()) / step;
            return true;
        }

        return false;
    }

    @Override
    public void pointerUp(int id, int button, float x, float y, float projectedX, float projectedY) {
        if (pointerId == id && pointerButton == button) {
            pointerId = -1;
            pointerButton = -1;
            acceleration = 0f;
            setValue(0f);
        }
    }

    @Override
    public void update(float deltaTime) {
        if (pointerId >= 0) {
            setValue(acceleration);
        }
    }

    public interface ValueChangedListener {
        void onValueChanged(float value);
    }
}
