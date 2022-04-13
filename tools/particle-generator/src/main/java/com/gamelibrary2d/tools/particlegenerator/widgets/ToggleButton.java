package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.AbstractPointerAwareGameObject;
import com.gamelibrary2d.opengl.shaders.ShaderParameter;
import com.gamelibrary2d.text.Font;
import com.gamelibrary2d.text.Label;

public class ToggleButton extends AbstractPointerAwareGameObject {
    private final Label label;

    private boolean toggled;
    private float defaultR;
    private float defaultG;
    private float defaultB;
    private float defaultA;
    private Rectangle bounds = Rectangle.EMPTY;

    public ToggleButton(Font font, String text) {
        this.label = new Label(font, text);
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
        if (toggled) {
            defaultR = label.getShaderParameter(ShaderParameter.COLOR_R);
            defaultG = label.getShaderParameter(ShaderParameter.COLOR_G);
            defaultB = label.getShaderParameter(ShaderParameter.COLOR_B);
            defaultA = label.getShaderParameter(ShaderParameter.ALPHA);
            label.setColor(Color.GREEN);
        } else {
            label.setColor(defaultR, defaultG, defaultB, defaultA);
        }
    }

    @Override
    protected void onPointerUp(int id, int button, float x, float y, float projectedX, float projectedY) {
        super.onPointerUp(id, button, x, y, projectedX, projectedY);
        setToggled(!isToggled());
    }

    public Label getLabel() {
        return label;
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