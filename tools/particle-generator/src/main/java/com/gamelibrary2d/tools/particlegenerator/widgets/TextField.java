package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.FocusManager;
import com.gamelibrary2d.Point;
import com.gamelibrary2d.KeyAndPointerState;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.denotations.*;
import com.gamelibrary2d.input.Keyboard;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.text.Label;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TextField
        extends AbstractGameObject
        implements FocusAware, PointerDownAware, PointerUpAware, PointerDownWhenFocusedAware, KeyDownAware, InputAware {
    private final Label label;
    private final List<FocusChangedListener> focusChangedListeners = new CopyOnWriteArrayList<>();
    private final Point pointerPosition = new Point();
    private Renderable background;
    private Rectangle bounds;
    private int pointerId = -1;
    private int pointerButton = -1;

    public TextField(Label label) {
        this.label = label;
    }

    @Override
    public void pointerDownWhenFocused(KeyAndPointerState state, int id, int button) {
        FocusManager.unfocus(this, false);
    }

    @Override
    public boolean pointerDown(KeyAndPointerState state, int id, int button, float x, float y) {
        pointerPosition.set(x, y, this);
        if (getBounds().contains(pointerPosition)) {
            pointerId = id;
            pointerButton = button;
            return true;
        }

        return false;
    }

    @Override
    public void pointerUp(KeyAndPointerState state, int id, int button, float x, float y) {
        if (pointerId == id && pointerButton == button) {
            pointerId = -1;
            pointerButton = -1;
            FocusManager.focus(this, false);
        }
    }

    @Override
    public void charInput(char charInput) {
        label.setText(label.getText() + charInput);
    }

    @Override
    public void keyDown(KeyAndPointerState state, int key, boolean repeat) {
        if (key == Keyboard.instance().keyBackspace()) {
            removeLast();
        }
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
    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    @Override
    protected void onRender(float alpha) {
        if (background != null) {
            background.render(alpha);
        }

        label.render(alpha);
    }

    @Override
    public void focused() {
        publishFocusChangedEvent(true);
    }

    @Override
    public void unfocused() {
        publishFocusChangedEvent(false);
    }

    private void publishFocusChangedEvent(boolean focused) {
        for (FocusChangedListener listener : focusChangedListeners) {
            listener.onFocusChanged(focused);
        }
    }

    public void addFocusChangedListener(FocusChangedListener listener) {
        focusChangedListeners.add(listener);
    }

    public interface FocusChangedListener {
        void onFocusChanged(boolean focused);
    }
}
