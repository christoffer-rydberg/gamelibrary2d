package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.PointerState;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;
import com.gamelibrary2d.functional.Action;
import com.gamelibrary2d.text.Label;

public class Button extends AbstractGameObject implements PointerDownAware, PointerUpAware {
    private final Action onClick;
    private final Label label;
    private final Point pointerPosition = new Point();
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
    public boolean pointerDown(PointerState pointerState, int id, int button, float transformedX, float transformedY) {
        pointerPosition.set(transformedX, transformedY, this);
        if (getBounds().contains(pointerPosition)) {
            pointerId = id;
            pointerButton = button;
            return true;
        }

        return false;
    }

    @Override
    public void pointerUp(PointerState pointerState, int id, int button, float transformedX, float transformedY) {
        if (pointerId == id && pointerButton == button) {
            pointerId = -1;
            pointerButton = -1;
            onClick.perform();
        }
    }
}