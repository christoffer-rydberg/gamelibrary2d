package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.functional.Action;
import com.gamelibrary2d.components.objects.AbstractPointerAwareGameObject;
import com.gamelibrary2d.demos.networkgame.client.resources.Fonts;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.glUtil.ModelMatrix;

public class Button extends AbstractPointerAwareGameObject {
    private final Action onClick;
    private final Rectangle bounds;
    private final Renderable background;
    private final ShadowedLabel label;

    public Button(ShadowedLabel label, Rectangle bounds, Action onClick) {
        this(label, null, bounds, onClick);
    }

    public Button(ShadowedLabel label, Renderable background, Rectangle bounds, Action onClick) {
        this.onClick = onClick;
        this.bounds = bounds;
        this.label = label;
        this.background = background;
    }

    @Override
    protected void onPointerUp(int id, int button, float x, float y, float projectedX, float projectedY) {
        onClick.perform();
    }

    @Override
    protected void onRender(float alpha) {
        if (background != null) {
            background.render(alpha);
        }

        ModelMatrix.instance().pushMatrix();
        float fontScale = Fonts.getFontScale();
        ModelMatrix.instance().scalef(fontScale, fontScale, 1f);
        label.render(alpha);
        ModelMatrix.instance().popMatrix();
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }
}