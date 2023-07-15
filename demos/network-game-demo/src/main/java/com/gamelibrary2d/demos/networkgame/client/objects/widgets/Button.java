package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.PointerState;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.functional.ParameterizedAction;

public class Button extends AbstractGameObject implements PointerDownAware, PointerUpAware {
    private final ParameterizedAction<Button> onClick;
    private final Rectangle bounds;
    private final ShadowedLabel label;
    private final Renderable background;
    private final Point pointerPosition = new Point();
    private int pointerId = -1;
    private int pointerButton = -1;

    public Button(ShadowedLabel label, Rectangle bounds, ParameterizedAction<Button> onClick) {
        this(label, null, bounds, onClick);
    }

    public Button(ShadowedLabel label, Renderable background, Rectangle bounds, ParameterizedAction<Button> onClick) {
        this.onClick = onClick;
        this.bounds = bounds;
        this.label = label;
        this.background = background;
    }

    public ShadowedLabel getLabel() {
        return label;
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
            onClick.perform(this);
        }
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }


    @Override
    protected void onRender(float alpha) {
        if (background != null) {
            background.render(alpha);
        }

        label.render(alpha);
    }
}