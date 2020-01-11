package com.gamelibrary2d.renderers;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.glUtil.ShaderProgram;
import com.gamelibrary2d.rendering.RenderSettings;
import com.gamelibrary2d.resources.Font;

public class TextRenderer extends AbstractShaderRenderer {

    private Font font;
    private String text = "";
    private int start;
    private int end;

    public TextRenderer(Font font) {
        setFont(font);
        updateSettings(RenderSettings.TEXTURED, 1);
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        start = 0;
        end = text.length();
    }

    public void setText(String text, int offset, int len) {
        this.text = text;
        start = offset;
        end = Math.min(text.length(), offset + len);
    }

    @Override
    public Rectangle getBounds() {
        return Rectangle.EMPTY;
    }

    @Override
    public void onRender(ShaderProgram shaderProgram) {
        font.render(shaderProgram, text, start, end);
    }

    @Override
    public boolean isVisible(float x, float y) {
        return false;
    }
}