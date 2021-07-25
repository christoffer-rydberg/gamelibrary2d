package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.FocusManager;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.denotations.InputAware;
import com.gamelibrary2d.components.denotations.KeyDownAware;
import com.gamelibrary2d.components.objects.AbstractPointerAwareGameObject;
import com.gamelibrary2d.demos.networkgame.client.resources.Fonts;
import com.gamelibrary2d.framework.Keyboard;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.glUtil.ModelMatrix;

public class InputField extends AbstractPointerAwareGameObject implements KeyDownAware, InputAware {
    private final Rectangle bounds;
    private final Renderable background;
    private final ShadowedLabel label;

    public InputField(ShadowedLabel label, Renderable background, Rectangle bounds) {
        this.bounds = bounds;
        this.label = label;
        this.background = background;
    }

    public int getIntValue() {
        return Integer.parseInt(label.getLabel().getText());
    }

    public String getStringValue() {
        return label.getLabel().getText();
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

        ModelMatrix.instance().pushMatrix();
        float fontScale = Fonts.getFontScale();
        ModelMatrix.instance().scalef(fontScale, fontScale, 1f);
        label.render(alpha);
        ModelMatrix.instance().popMatrix();
    }

    @Override
    protected void onPointerUp(int id, int button, float x, float y, float projectedX, float projectedY) {
        FocusManager.focus(this, false);
    }

    @Override
    public void charInput(char charInput) {
        label.getLabel().setText(label.getLabel().getText() + charInput);
    }

    @Override
    public void keyDown(int key, boolean repeat) {
        if (key == Keyboard.instance().keyBackspace()) {
            removeLast();
        }
    }

    private void removeLast() {
        String text = label.getLabel().getText();
        int textLength = text.length();
        if (textLength > 0) {
            label.getLabel().setText(text.substring(0, textLength - 1));
        }
    }
}
