package com.gamelibrary2d.text;

import com.gamelibrary2d.opengl.renderers.Renderer;

public interface TextRenderer extends Renderer {
    void render(float alpha, String text, int offset, int len);
}
