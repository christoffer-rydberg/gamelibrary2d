package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.InputState;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;
import com.gamelibrary2d.demos.networkgame.client.DemoGame;
import com.gamelibrary2d.demos.networkgame.client.input.Controller;
import com.gamelibrary2d.demos.networkgame.client.input.ControllerInputId;
import com.gamelibrary2d.demos.networkgame.client.objects.network.LocalPlayer;
import com.gamelibrary2d.demos.networkgame.client.options.RotationMode;
import com.gamelibrary2d.demos.networkgame.client.settings.Dimensions;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RotationArea implements Renderable, PointerDownAware, PointerMoveAware, PointerUpAware {
    private final DemoGame game;
    private final LocalPlayer player;
    private final Rectangle bounds;
    private final Rectangle lowerBounds;
    private final float minNodeInterval = 5f * Dimensions.getContentScaleX();
    private final float maxLeftRightDistance = 10f * Dimensions.getContentScaleX();
    private final List<ValueChangedListener> valueChangedListeners = new CopyOnWriteArrayList<>();

    private final Point start = new Point();
    private int pointerId = -1;
    private int pointerButton = -1;
    private float direction;
    private float value;

    private RotationArea(DemoGame game, Rectangle bounds, LocalPlayer player) {
        this.game = game;
        this.bounds = bounds;
        this.lowerBounds = new Rectangle(bounds.getLowerX(), bounds.getLowerY(), bounds.getUpperX(), bounds.getUpperY() / 10f);
        this.player = player;
    }

    public static RotationArea create(DemoGame game, Rectangle bounds, LocalPlayer player) {
        return new RotationArea(
                game,
                bounds,
                player);
    }

    public RotationMode getMode() {
        return game.getOptions().getRotationMode();
    }

    public void addValueChangedListener(ValueChangedListener listener) {
        valueChangedListeners.add(listener);
    }

    public void removeValueChangedListener(ValueChangedListener listener) {
        valueChangedListeners.remove(listener);
    }

    @Override
    public void render(float alpha) {

    }

    @Override
    public boolean pointerDown(InputState inputState, int id, int button, float x, float y) {
        if (lowerBounds.contains(x, y)) {
            switch (getMode()) {
                case LEFT_OR_RIGHT:
                    game.getOptions().setRotationMode(RotationMode.TOWARD_DIRECTION);
                    break;
                case TOWARD_DIRECTION:
                    game.getOptions().setRotationMode(RotationMode.LEFT_OR_RIGHT);
                    break;
            }
            return true;
        } else if (bounds.contains(x, y)) {
            if (pointerId < 0) {
                pointerId = id;
                pointerButton = button;
                start.set(x, y);
                if (getMode() == RotationMode.TOWARD_DIRECTION) {
                    setValue(1f);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean pointerMove(InputState inputState, int id, float x, float y) {
        if (pointerId == id) {
            switch (getMode()) {
                case LEFT_OR_RIGHT:
                    direction = start.getDirectionDegrees(x, y);
                    float value = Math.min(1f, start.getDistance(x, start.getY()) / maxLeftRightDistance);
                    setLeftRightControllerValue(direction >= 0 ? value : -value);
                    break;
                case TOWARD_DIRECTION:
                    if (start.getDistance(x, y) > minNodeInterval) {
                        direction = start.getDirectionDegrees(x, y);
                        start.set(x, y);
                        player.rotateTowardsGoal(Math.round(direction));
                    }
                    break;
            }

            return true;
        }

        return false;
    }

    @Override
    public void swallowedPointerMove(InputState inputState, int id) {

    }

    private void reset() {
        pointerId = -1;
        pointerButton = -1;
        direction = 0;
        switch (getMode()) {
            case LEFT_OR_RIGHT:
                setLeftRightControllerValue(0f);
                break;
            case TOWARD_DIRECTION:
                setValue(0f);
                break;
        }
    }

    @Override
    public void pointerUp(InputState inputState, int id, int button, float x, float y) {
        if (pointerId == id && pointerButton == button) {
            reset();
        }
    }

    private void setValue(float value) {
        if (this.value != value) {
            this.value = value;
            for (ValueChangedListener listener : valueChangedListeners) {
                listener.onValueChanged(value);
            }
        }
    }

    private void setLeftRightControllerValue(float value) {
        Controller controller = player.getController();

        if (value < 0) {
            controller.setValue(ControllerInputId.RIGHT, 0f);
            controller.setValue(ControllerInputId.LEFT, Math.abs(value));
        } else {
            controller.setValue(ControllerInputId.LEFT, 0f);
            controller.setValue(ControllerInputId.RIGHT, value);
        }

        setValue(value);
    }

    public interface ValueChangedListener {
        void onValueChanged(float value);
    }
}
