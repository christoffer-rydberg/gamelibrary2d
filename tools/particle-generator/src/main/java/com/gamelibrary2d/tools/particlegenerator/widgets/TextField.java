package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.FocusManager;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.objects.AbstractObservableGameObject;
import com.gamelibrary2d.renderers.Label;
import com.gamelibrary2d.framework.Keyboard;
import com.gamelibrary2d.framework.Renderable;

public class TextField extends AbstractObservableGameObject {
    private final Label label;
    private Renderable background;
    private Rectangle bounds;

    public TextField(Label label) {
        this.label = label;
    }

    @Override
    protected void onPointerUp(int id, int button, float x, float y, float projectedX, float projectedY) {
        FocusManager.focus(this, false);
    }

    @Override
    public void charInput(char charInput) {
        label.setText(label.getText() + charInput);
    }

    @Override
    public void keyDown(int key, boolean repeat) {
        if (key == Keyboard.instance().keyBackspace()) {
            removeLast();
        }
    }

    @Override
    public void keyUp(int key) {

    }

    private void removeLast() {
        String text = label.getText();
        int textLength = text.length();
        if (textLength > 0) {
            label.setText(text.substring(0, textLength - 1));
        }
    }

    public void setBackground(Renderable background) {
        this.background = background;
    }

    @Override
    protected void onRender(float alpha) {
        if (background != null) {
            background.render(alpha);
        }

        label.render(alpha);
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }
}
