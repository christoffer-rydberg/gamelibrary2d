package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.functional.ParameterizedAction;
import com.gamelibrary2d.components.objects.AbstractPointerAwareGameObject;
import com.gamelibrary2d.framework.Renderable;

public class Button extends AbstractPointerAwareGameObject {
    private final ParameterizedAction<Button> onClick;
    private final Rectangle bounds;
    private final Renderable background;
    private final ShadowedLabel label;

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
    protected void onPointerUp(int id, int button, float x, float y, float projectedX, float projectedY) {
        onClick.perform(this);
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
}