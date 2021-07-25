package com.gamelibrary2d.particle.renderers;

import com.gamelibrary2d.glUtil.OpenGLBuffer;
import com.gamelibrary2d.particle.systems.ParticleSystem;

public interface ParticleRenderer {

    void render(ParticleSystem particleSystem, OpenGLBuffer renderBuffer, boolean gpuOutdated, int offset, int len, float alpha);

}
