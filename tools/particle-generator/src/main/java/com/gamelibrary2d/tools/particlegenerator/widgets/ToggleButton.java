package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.widgets.AbstractWidget;
import com.gamelibrary2d.components.widgets.Label;
import com.gamelibrary2d.glUtil.ShaderParameter;
import com.gamelibrary2d.resources.Font;

public class ToggleButton extends AbstractWidget<Label> {
    private boolean toggled;
    private float defaultR;
    private float defaultG;
    private float defaultB;
    private float defaultA;

    public ToggleButton(Font font, String text) {
        setContent(new Label(font, text));
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
        if (toggled) {
            defaultR = getContent().getShaderParameter(ShaderParameter.COLOR_R);
            defaultG = getContent().getShaderParameter(ShaderParameter.COLOR_G);
            defaultB = getContent().getShaderParameter(ShaderParameter.COLOR_B);
            defaultA = getContent().getShaderParameter(ShaderParameter.ALPHA);
            getContent().setColor(Color.GREEN);
        } else {
            getContent().setColor(defaultR, defaultG, defaultB, defaultA);
        }
    }

    @Override
    protected void onPointerUp(int id, int button, float x, float y, float projectedX, float projectedY) {
        super.onPointerUp(id, button, x, y, projectedX, projectedY);
        setToggled(!isToggled());
    }

    @Override
    public Label getContent() {
        return super.getContent();
    }

    @Override
    public void setContent(Label content) {
        super.setContent(content);
    }

    @Override
    public void setBounds(Rectangle bounds) {
        super.setBounds(bounds);
    }
}