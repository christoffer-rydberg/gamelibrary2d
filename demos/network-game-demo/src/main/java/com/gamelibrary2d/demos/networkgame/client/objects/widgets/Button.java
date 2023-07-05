package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.AbstractPointerAwareGameObject;
import com.gamelibrary2d.functional.ParameterizedAction;

public class Button extends AbstractPointerAwareGameObject {
    private final ParameterizedAction<Button> onClick;
    private final Rectangle bounds;
    private final ShadowedLabel label;
    private final Renderable background;
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
            onClick.perform(this);
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