package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.demos.networkgame.client.resources.Fonts;
import com.gamelibrary2d.demos.networkgame.client.settings.Dimensions;
import com.gamelibrary2d.opengl.ModelMatrix;
import com.gamelibrary2d.opengl.shaders.ShaderParameter;
import com.gamelibrary2d.text.Label;

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

    private void renderShadow(float fontScale, float alpha) {
        float r = label.getShaderParameter(ShaderParameter.COLOR_R);
        float g = label.getShaderParameter(ShaderParameter.COLOR_G);
        float b = label.getShaderParameter(ShaderParameter.COLOR_B);
        float a = label.getShaderParameter(ShaderParameter.ALPHA);

        ModelMatrix.instance().pushMatrix();
        try {
            label.setColor(shadowColor);

            ModelMatrix.instance().translatef(
                    -Dimensions.getContentScaleX() / 2f,
                    -Dimensions.getContentScaleY() / 2f,
                    0);

            ModelMatrix.instance().scalef(fontScale, fontScale, 1f);

            label.render(alpha);
        } finally {
            ModelMatrix.instance().popMatrix();
            label.setColor(r, g, b, a);
        }
    }

    private void renderLabel(float fontScale, float alpha) {
        ModelMatrix.instance().pushMatrix();
        try {
            ModelMatrix.instance().scalef(fontScale, fontScale, 1f);
            label.render(alpha);
        } finally {
            ModelMatrix.instance().popMatrix();
        }
    }

    public void render(float alpha) {
        float fontScale = Fonts.getFontScale();
        renderShadow(fontScale, alpha);
        renderLabel(fontScale, alpha);
    }
}
