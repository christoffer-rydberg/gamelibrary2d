package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.FocusManager;
import com.gamelibrary2d.PointerState;
import com.gamelibrary2d.components.denotations.PointerDownWhenFocusedAware;
import com.gamelibrary2d.input.Keyboard;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.AbstractPointerAwareGameObject;
import com.gamelibrary2d.components.denotations.InputAware;
import com.gamelibrary2d.components.denotations.KeyDownAware;

public class InputField
        extends AbstractPointerAwareGameObject
        implements PointerDownWhenFocusedAware, KeyDownAware, InputAware {
    private final Rectangle bounds;
    private final ShadowedLabel label;
    private final Renderable background;

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
    public void pointerDownWhenFocused(PointerState pointerState, int id, int button) {
        FocusManager.unfocus(this, false);
    }

    @Override
    protected boolean onPointerDown(int id, int button, float transformedX, float transformedY) {
        return true;
    }

    @Override
    protected void onPointerUp(int id, int button, float transformedX, float transformedY) {
        FocusManager.focus(this, false);
    }

    @Override
    protected boolean isTrackingPointerPositions() {
        return false;
    }

    @Override
    protected void onPointerEntered(int id) {

    }

    @Override
    protected void onPointerLeft(int id) {

    }

    @Override
    protected boolean onPointerMove(int id, float transformedX, float transformedY) {
        return false;
    }

    @Override
    public void charInput(char charInput) {
        label.getLabel().setText(label.getLabel().getText() + charInput);
    }

    @Override
    public void keyDown(int key, boolean repeat) {
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
