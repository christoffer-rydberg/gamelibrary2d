package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.functional.Action;
import com.gamelibrary2d.components.objects.AbstractPointerAwareGameObject;
import com.gamelibrary2d.renderers.Label;

public class Button extends AbstractPointerAwareGameObject {
    private final Action onClick;
    private final Label label;
    private Rectangle bounds = Rectangle.EMPTY;

    public Button(Label label, Action onClick) {
        this.label = label;
        this.onClick = onClick;
    }

    @Override
    protected void onPointerUp(int id, int button, float x, float y, float projectedX, float projectedY) {
        super.onPointerUp(id, button, x, y, projectedX, projectedY);
        onClick.perform();
    }

    @Override
    protected void onRender(float alpha) {
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