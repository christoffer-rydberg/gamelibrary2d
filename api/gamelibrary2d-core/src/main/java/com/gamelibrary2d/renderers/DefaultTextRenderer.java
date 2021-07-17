package com.gamelibrary2d.renderers;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.glUtil.ModelMatrix;
import com.gamelibrary2d.glUtil.OpenGLUtils;
import com.gamelibrary2d.glUtil.ShaderParameter;
import com.gamelibrary2d.glUtil.ShaderProgram;
import com.gamelibrary2d.resources.BlendMode;
import com.gamelibrary2d.resources.Font;
import com.gamelibrary2d.resources.HorizontalTextAlignment;
import com.gamelibrary2d.resources.VerticalTextAlignment;

public class DefaultTextRenderer extends AbstractRenderer implements TextRenderer {
    private Font font;
    private HorizontalTextAlignment horizontalAlignment = HorizontalTextAlignment.CENTER;
    private VerticalTextAlignment verticalAlignment = VerticalTextAlignment.CENTER;
    private ShaderProgram shaderProgram;
    private BlendMode blendMode = BlendMode.TRANSPARENT;

    public DefaultTextRenderer() {

    }

    public DefaultTextRenderer(Font font) {
        setFont(font);
    }

    public ShaderProgram getShaderProgram() {
        return shaderProgram != null ? shaderProgram : ShaderProgram.getDefaultShaderProgram();
    }

    public void setShaderProgram(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Rectangle calculateBounds(String text, int offset, int length) {
        float textWidth = font.getTextWidth(text, offset, length);
        float offsetX = offsetFromHorizontalAlignment(textWidth);
        float offsetY = offsetFromVerticalAlignment(font);
        return new Rectangle(
                offsetX,
                offsetY - font.getDescent(),
                textWidth + offsetX,
                offsetY + font.getAscent());
    }

    protected void applyParameters(float alpha) {
        setShaderParameter(ShaderParameter.TEXTURED, 1);
        float alphaSetting = getShaderParameter(ShaderParameter.ALPHA);

        try {
            ShaderProgram program = getShaderProgram();
            setShaderParameter(ShaderParameter.ALPHA, alphaSetting * alpha);
            applyShaderParameters(program);
            program.applyParameters();
        } finally {
            setShaderParameter(ShaderParameter.ALPHA, alphaSetting);
        }
    }

    @Override
    public void render(float alpha, String text, int offset, int len) {
        ShaderProgram shaderProgram = getShaderProgram();
        shaderProgram.bind();
        shaderProgram.updateModelMatrix(ModelMatrix.instance());
        applyParameters(alpha);
        OpenGLUtils.setBlendMode(blendMode);

        float textWidth = font.getTextWidth(text, offset, len);
        float xOffset = offsetFromHorizontalAlignment(textWidth);
        float yOffset = offsetFromVerticalAlignment(font);

        if (xOffset != 0 || yOffset != 0) {
            ModelMatrix.instance().pushMatrix();
            ModelMatrix.instance().translatef(xOffset, yOffset, 0);
        }

        font.render(shaderProgram, text, offset, len);

        if (xOffset != 0 || yOffset != 0) {
            ModelMatrix.instance().popMatrix();
        }
    }

    public HorizontalTextAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalTextAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public VerticalTextAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalTextAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public void setAlignment(HorizontalTextAlignment horizontal, VerticalTextAlignment vertical) {
        setHorizontalAlignment(horizontal);
        setVerticalAlignment(vertical);
    }

    private float offsetFromHorizontalAlignment(float size) {
        switch (horizontalAlignment) {
            case RIGHT:
                return -size;
            case CENTER:
                return -size / 2;
            default:
                return 0;
        }
    }

    private float offsetFromVerticalAlignment(Font font) {
        switch (verticalAlignment) {
            case TOP:
                return -font.getAscent();
            case CENTER:
                return (font.getAscent() + font.getDescent()) / 2f - font.getAscent();
            case BOTTOM:
                return font.getDescent();
            case BASE_LINE:
            default:
                return 0;
        }
    }
}