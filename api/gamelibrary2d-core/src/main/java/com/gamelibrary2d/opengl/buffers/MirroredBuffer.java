package com.gamelibrary2d.opengl.buffers;

public interface MirroredBuffer extends OpenGLBuffer {

    void updateGPU(int offset, int len);

    void updateCPU(int offset, int len);

    void copy(int offset, int destination, int len);
}