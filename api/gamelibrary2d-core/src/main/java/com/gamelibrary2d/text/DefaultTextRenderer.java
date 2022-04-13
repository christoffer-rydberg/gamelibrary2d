package com.gamelibrary2d.text;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.opengl.ModelMatrix;
import com.gamelibrary2d.opengl.OpenGLState;
import com.gamelibrary2d.opengl.renderers.AbstractRenderer;
import com.gamelibrary2d.opengl.renderers.BlendMode;
import com.gamelibrary2d.opengl.shaders.ShaderParameter;
import com.gamelibrary2d.opengl.shaders.ShaderProgram;

public class DefaultTextRenderer extends AbstractRenderer implements TextRenderer {
    private Font font;
    private BlendMode blendMode = BlendMode.TRANSPARENT;
    private HorizontalTextAlignment horizontalAlignment = HorizontalTextAlignment.CENTER;
    private VerticalTextAlignment verticalAlignment = VerticalTextAlignment.CENTER;

    public DefaultTextRenderer() {

    }

    public DefaultTextRenderer(Font font) {
        setFont(font);
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
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

    @Override
    public void render(float alpha, String text, int offset, int len) {
        ShaderProgram shaderProgram = prepareShaderProgram(alpha);
        OpenGLState.setBlendMode(blendMode);
        onRender(shaderProgram, text, offset, len);
    }

    @Override
    protected ShaderProgram prepareShaderProgram(float alpha) {
        setShaderParameter(ShaderParameter.TEXTURED, 1);
        return super.prepareShaderProgram(alpha);
    }

    private void onRender(ShaderProgram shaderProgram, String text, int offset, int len) {
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