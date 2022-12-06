package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.AbstractPointerAwareGameObject;
import com.gamelibrary2d.functional.ParameterizedAction;

public class Button extends AbstractPointerAwareGameObject {
    private final ParameterizedAction<Button> onClick;
    private final Rectangle bounds;
    private final ShadowedLabel label;
    private final Renderable renderer;

    public Button(ShadowedLabel label, Rectangle bounds, ParameterizedAction<Button> onClick) {
        this(label, null, bounds, onClick);
    }

    public Button(ShadowedLabel label, Renderable background, Rectangle bounds, ParameterizedAction<Button> onClick) {
        this.onClick = onClick;
        this.bounds = bounds;
        this.label = label;
        this.renderer = alpha -> {
            if (background != null) {
                background.render(alpha);
            }

            label.render(alpha);
        };
    }

    public ShadowedLabel getLabel() {
        return label;
    }

    @Override
    protected void onPointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {
        onClick.perform(this);
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public Renderable getRenderer() {
        return renderer;
    }
}