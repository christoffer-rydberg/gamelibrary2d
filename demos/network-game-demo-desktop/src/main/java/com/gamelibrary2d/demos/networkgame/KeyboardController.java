package com.gamelibrary2d.demos.networkgame;

import com.gamelibrary2d.input.Keyboard;
import com.gamelibrary2d.demos.networkgame.client.input.AbstractController;
import com.gamelibrary2d.demos.networkgame.client.input.ControllerInputId;
import com.gamelibrary2d.demos.networkgame.client.input.VirtualController;

public class KeyboardController extends AbstractController {
    private final int FIXED_STEPS = 10;
    private final VirtualController virtualController = new VirtualController();

    private static int convertToKeyboardInputId(ControllerInputId id) {
        switch (id) {
            case LEFT:
                return Keyboard.instance().keyLeft();
            case RIGHT:
                return Keyboard.instance().keyRight();
            case UP:
                return Keyboard.instance().keyUp();
            default:
                throw new IllegalStateException("Unexpected value: " + id);
        }
    }

    private float getExactValue(ControllerInputId id) {
        float virtualValue = virtualController.getValue(id);
        if (virtualValue == 0f) {
            int keyboardInputId = convertToKeyboardInputId(id);
            return Keyboard.instance().isKeyDown(keyboardInputId) ? 1f : 0f;
        } else {
            return virtualValue;
        }
    }

    @Override
    public float getValue(ControllerInputId id) {
        return Math.round(getExactValue(id) * FIXED_STEPS) / (float) FIXED_STEPS;
    }

    @Override
    public void setValue(ControllerInputId id, float value) {
        virtualController.setValue(id, value);
    }
}
