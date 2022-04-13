package com.gamelibrary2d.opengl.renderers;

import com.gamelibrary2d.opengl.buffers.OpenGLBuffer;

public interface ArrayRenderer<T extends OpenGLBuffer> extends Renderer {
    void render(float alpha, T array, int offset, int len);
}
