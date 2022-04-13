package com.gamelibrary2d.particles;

import com.gamelibrary2d.opengl.buffers.OpenGLBuffer;

public interface ParticleRenderer {

    void render(Object particleSystem, OpenGLBuffer renderBuffer, boolean gpuOutdated, int offset, int len, float alpha);

}
