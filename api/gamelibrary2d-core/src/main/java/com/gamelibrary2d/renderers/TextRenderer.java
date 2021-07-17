package com.gamelibrary2d.renderers;

public interface TextRenderer extends Renderer {
    void render(float alpha, String text, int offset, int len);
}
