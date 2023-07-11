package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.AbstractPointerAwareGameObject;
import com.gamelibrary2d.functional.Action;
import com.gamelibrary2d.text.Label;

public class Button extends AbstractPointerAwareGameObject {
    private final Action onClick;
    private final Label label;
    private Rectangle bounds = Rectangle.EMPTY;

    private int pointerId = -1;
    private int pointerButton = -1;

    public Button(Label label, Action onClick) {
        this.label = label;
        this.onClick = onClick;
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
        label.render(alpha);
    }

    @Override
    protected boolean onPointerDown(int id, int button, float transformedX, float transformedY) {
        pointerId = id;
        pointerButton = button;
        return true;
    }

    @Override
    protected void onPointerUp(int id, int button, float transformedX, float transformedY) {
        if (pointerId == id && pointerButton == button) {
            pointerId = -1;
            pointerButton = -1;
            onClick.perform();
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
    protected boolean onPointerMove(int id, float transformedX, float transformedY) {
        return false;
    }
}