package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.FocusManager;
import com.gamelibrary2d.components.AbstractPointerAwareGameObject;
import com.gamelibrary2d.components.denotations.FocusAware;
import com.gamelibrary2d.components.denotations.InputAware;
import com.gamelibrary2d.components.denotations.KeyDownAware;
import com.gamelibrary2d.components.denotations.PointerDownWhenFocusedAware;
import com.gamelibrary2d.input.Keyboard;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.text.Label;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TextField
        extends AbstractPointerAwareGameObject
        implements FocusAware, PointerDownWhenFocusedAware, KeyDownAware, InputAware {
    private final Label label;
    private final List<FocusChangedListener> focusChangedListeners = new CopyOnWriteArrayList<>();
    private Renderable background;
    private Rectangle bounds;
    private int pointerId = -1;
    private int pointerButton = -1;

    public TextField(Label label) {
        this.label = label;
    }

    @Override
    public void pointerDownWhenFocused(int id, int button) {
        FocusManager.unfocus(this, false);
    }

    @Override
    protected boolean onPointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {
        pointerId = id;
        pointerButton = button;
        return true;
    }

    @Override
    protected void onPointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {
        if (pointerId == id && pointerButton == button) {
            pointerId = -1;
            pointerButton = -1;
            FocusManager.focus(this, false);
        }
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
    protected boolean onPointerMove(int id, float x, float y, float transformedX, float transformedY) {
        return false;
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
