package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.FocusManager;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.AbstractPointerAwareGameObject;
import com.gamelibrary2d.components.denotations.InputAware;
import com.gamelibrary2d.components.denotations.KeyDownAware;
import com.gamelibrary2d.framework.Keyboard;
import com.gamelibrary2d.framework.Renderable;

public class InputField extends AbstractPointerAwareGameObject implements KeyDownAware, InputAware {
    private final Rectangle bounds;
    private final ShadowedLabel label;
    private final Renderable renderer;

    public InputField(ShadowedLabel label, Renderable background, Rectangle bounds) {
        this.bounds = bounds;
        this.label = label;
        this.renderer = alpha -> {
            if (background != null) {
                background.render(alpha);
            }

            label.render(alpha);
        };
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
    protected void onPointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {
        FocusManager.focus(this, false);
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
    public Renderable getRenderer() {
        return renderer;
    }
}
