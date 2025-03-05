package com.gamelibrary2d.text;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;

public class Label implements ContentRenderer {
    private final TextRenderer textRenderer;
    private String text;
    private Rectangle bounds;

    public Label(Font font) {
        this(font, null);
    }

    public Label(Font font, String text) {
        textRenderer = new TextRenderer(font);
        textRenderer.setAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.CENTER);
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public HorizontalTextAlignment getHorizontalAlignment() {
        return textRenderer.getHorizontalAlignment();
    }

    public void setHorizontalAlignment(HorizontalTextAlignment horizontalAlignment) {
        if (textRenderer.getHorizontalAlignment() != horizontalAlignment) {
            textRenderer.setHorizontalAlignment(horizontalAlignment);
            bounds = null;
        }
    }

    public VerticalTextAlignment getVerticalAlignment() {
        return textRenderer.getVerticalAlignment();
    }

    public void setVerticalAlignment(VerticalTextAlignment verticalAlignment) {
        if (textRenderer.getVerticalAlignment() != verticalAlignment) {
            textRenderer.setVerticalAlignment(verticalAlignment);
            bounds = null;
        }
    }

    public void setAlignment(HorizontalTextAlignment horizontal, VerticalTextAlignment vertical) {
        setHorizontalAlignment(horizontal);
        setVerticalAlignment(vertical);
    }

    @Override
    public float getShaderParameter(int index) {
        return textRenderer.getShaderParameter(index);
    }

    @Override
    public void setShaderParameter(int index, float value) {
        textRenderer.setShaderParameter(index, value);
    }

    @Override
    public Rectangle getBounds() {
        if (text == null) {
            return Rectangle.EMPTY;
        }

        if (bounds == null) {
            bounds = textRenderer.calculateBounds(text, 0, text.length());
        }

        return bounds;
    }

    @Override
    public void render(float alpha) {
        if (text == null) {
            return;
        }

        textRenderer.render(text, alpha, 0, text.length());
    }
}