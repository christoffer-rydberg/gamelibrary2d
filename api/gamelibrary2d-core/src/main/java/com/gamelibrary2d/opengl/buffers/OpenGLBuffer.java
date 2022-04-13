package com.gamelibrary2d.opengl.buffers;

public interface OpenGLBuffer {

    void bind();

    void unbind();

    int getBufferId();

    int getCapacity();
}