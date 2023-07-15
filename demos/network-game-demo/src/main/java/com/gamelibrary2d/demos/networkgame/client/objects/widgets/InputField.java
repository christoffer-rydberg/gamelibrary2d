package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.FocusManager;
import com.gamelibrary2d.Point;
import com.gamelibrary2d.InputState;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerDownWhenFocusedAware;
import com.gamelibrary2d.input.Keyboard;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.denotations.InputAware;
import com.gamelibrary2d.components.denotations.KeyDownAware;

public class InputField
        extends AbstractGameObject
        implements PointerDownAware, PointerDownWhenFocusedAware, KeyDownAware, InputAware {
    private final Rectangle bounds;
    private final ShadowedLabel label;
    private final Renderable background;
    private final Point pointerPosition = new Point();

    public InputField(ShadowedLabel label, Renderable background, Rectangle bounds) {
        this.bounds = bounds;
        this.label = label;
        this.background = background;
    }

    public int getIntValue() {
        return Integer.parseInt(label.getLabel().getText());
    }

    public String getStringValue() {
        return label.getLabel().getText();
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void pointerDownWhenFocused(InputState inputState, int id, int button) {
        FocusManager.unfocus(this, false);
    }

    @Override
    public boolean pointerDown(InputState inputState, int id, int button, float x, float y) {
        pointerPosition.set(x, y, this);
        if (getBounds().contains(pointerPosition)) {
            FocusManager.focus(this, false);
            return true;
        }

        return false;
    }

    @Override
    public void charInput(char charInput) {
        label.getLabel().setText(label.getLabel().getText() + charInput);
    }

    @Override
    public void keyDown(InputState inputState, int key, boolean repeat) {
        if (key == Keyboard.instance().keyBackspace()) {
            removeLast();
        }
    }

    private void removeLast() {
        String text = label.getLabel().getText();
        int textLength = text.length();
        if (textLength > 0) {
            label.getLabel().setText(text.substring(0, textLength - 1));
        }
    }

    @Override
    protected void onRender(float alpha) {
        if (background != null) {
            background.render(alpha);
        }

        label.render(alpha);
    }
}
