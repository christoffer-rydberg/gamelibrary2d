package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.common.FloatUtils;
import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;
import com.gamelibrary2d.components.denotations.Updatable;
import com.gamelibrary2d.demos.networkgame.client.input.Controller;
import com.gamelibrary2d.demos.networkgame.client.input.ControllerInputId;
import com.gamelibrary2d.demos.networkgame.client.objects.network.LocalPlayer;
import com.gamelibrary2d.demos.networkgame.client.settings.Dimensions;
import com.gamelibrary2d.framework.Renderable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RotationArea implements Renderable, PointerDownAware, PointerMoveAware, PointerUpAware, Updatable {
    private final LocalPlayer player;
    private final float max;
    private final RotationMode mode;
    private final Rectangle bounds;
    private final List<ValueChangedListener> valueChangedListeners = new CopyOnWriteArrayList<>();

    private final float minNodeInterval = 5f * Dimensions.getContentScaleX();
    private final Point start = new Point();
    private int pointerId = -1;
    private int pointerButton = -1;
    private float direction;
    private float rotationSpeed;
    private float value;

    private RotationArea(RotationMode mode, Rectangle bounds, LocalPlayer player, float maxDistance) {
        this.mode = mode;
        this.bounds = bounds;
        this.player = player;
        this.max = maxDistance;
    }

    public static RotationArea create(RotationMode mode, Rectangle bounds, LocalPlayer player, float maxDistance, Disposer disposer) {
        return new RotationArea(
                mode,
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
        float controllerValue = Math.max(-max, Math.min(max, value)) / max;
        if (this.value != controllerValue) {
            this.value = controllerValue;
            Controller controller = player.getController();

            if (mode == RotationMode.TOWARD_DIRECTION) {
                player.setAccelerationLimit(1f - Math.abs(controllerValue));
            }

            if (controllerValue < 0) {
                controller.setValue(ControllerInputId.RIGHT, 0f);
                controller.setValue(ControllerInputId.LEFT, Math.abs(controllerValue));
            } else {
                controller.setValue(ControllerInputId.LEFT, 0f);
                controller.setValue(ControllerInputId.RIGHT, controllerValue);
            }

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
                start.set(projectedX, projectedY);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean pointerMove(int id, float x, float y, float projectedX, float projectedY) {
        if (pointerId == id) {
            switch (mode) {
                case LEFT_OR_RIGHT:
                    direction = start.getDirectionDegrees(projectedX, projectedY);
                    rotationSpeed = Math.min(max, start.getDistance(projectedX, start.getY()));
                    break;
                case TOWARD_DIRECTION:
                    if (start.getDistance(projectedX, projectedY) > minNodeInterval) {
                        rotationSpeed = max;
                        direction = start.getDirectionDegrees(projectedX, projectedY);
                        start.set(projectedX, projectedY);
                    }
                    break;
            }

            return true;
        }

        return false;
    }

    @Override
    public void pointerUp(int id, int button, float x, float y, float projectedX, float projectedY) {
        if (pointerId == id && pointerButton == button) {
            pointerId = -1;
            pointerButton = -1;
            if (mode == RotationMode.LEFT_OR_RIGHT) {
                direction = 0;
                rotationSpeed = 0;
                setValue(0f);
            }
        }
    }

    @Override
    public void update(float deltaTime) {
        switch (mode) {
            case LEFT_OR_RIGHT:
                if (pointerId >= 0) {
                    setValue(direction >= 0 ? rotationSpeed : -rotationSpeed);
                    break;
                }
            case TOWARD_DIRECTION:
                float delta = FloatUtils.normalizeDegrees(direction - player.getRotation());
                setValue((delta / 180f) * rotationSpeed * 2);
                break;
        }
    }

    public enum RotationMode {
        LEFT_OR_RIGHT,
        TOWARD_DIRECTION
    }

    public interface ValueChangedListener {
        void onValueChanged(float value);
    }
}
