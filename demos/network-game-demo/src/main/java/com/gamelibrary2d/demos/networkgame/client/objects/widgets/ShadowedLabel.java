package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.demos.networkgame.client.settings.Dimensions;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.glUtil.ModelMatrix;
import com.gamelibrary2d.glUtil.ShaderParameter;
import com.gamelibrary2d.renderers.Label;

public class ShadowedLabel implements Renderable {
    private final Label label;
    private final Color shadowColor;

    public ShadowedLabel(Label label, Color shadowColor) {
        this.label = label;
        this.shadowColor = shadowColor;
    }

    public Label getLabel() {
        return label;
    }

    public void render(float alpha) {
        float r = label.getShaderParameter(ShaderParameter.COLOR_R);
        float g = label.getShaderParameter(ShaderParameter.COLOR_G);
        float b = label.getShaderParameter(ShaderParameter.COLOR_B);
        float a = label.getShaderParameter(ShaderParameter.ALPHA);
        ModelMatrix.instance().pushMatrix();
        try {
            label.setColor(shadowColor);
            ModelMatrix.instance().translatef(-Dimensions.getContentScaleX() / 2f, -Dimensions.getContentScaleY() / 2f, 0);
            label.render(alpha);
        } finally {
            ModelMatrix.instance().popMatrix();
            label.setColor(r, g, b, a);
        }

        label.render(alpha);
    }
}
