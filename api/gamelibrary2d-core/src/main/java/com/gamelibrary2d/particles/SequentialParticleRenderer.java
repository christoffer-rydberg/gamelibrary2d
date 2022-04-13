package com.gamelibrary2d.particles;

import com.gamelibrary2d.opengl.ModelMatrix;
import com.gamelibrary2d.opengl.buffers.OpenGLBuffer;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;
import com.gamelibrary2d.opengl.shaders.ShaderParameter;

public class SequentialParticleRenderer implements ParticleRenderer {
    private ContentRenderer renderer;

    public SequentialParticleRenderer(ContentRenderer renderer) {
        this.renderer = renderer;
    }

    public ContentRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(ContentRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void render(Object particleSystem, OpenGLBuffer buffer, boolean gpuOutdated, int offset, int len, float alpha) {
        if (!(buffer instanceof ParticleRenderBuffer)) {
            throw new IllegalArgumentException("Buffer must be of type ParticleRenderBuffer");
        }

        if (renderer != null) {
            ParticleRenderBuffer renderBuffer = (ParticleRenderBuffer) buffer;

            ModelMatrix modelMatrix = ModelMatrix.instance();

            int end = offset + len;
            for (int i = offset; i < end; ++i) {
                int renderOffset = i * renderBuffer.getStride();

                modelMatrix.pushMatrix();

                modelMatrix.translatef(
                        renderBuffer.getPosX(renderOffset),
                        renderBuffer.getPosY(renderOffset),
                        0);

                modelMatrix.rotatef(-renderBuffer.getRotation(renderOffset), 0, 0, 1);

                modelMatrix.scalef(
                        renderBuffer.getScale(renderOffset),
                        renderBuffer.getScale(renderOffset),
                        1.0f);

                if (particleSystem instanceof DefaultParticleSystem) {
                    renderer.setShaderParameter(
                            ShaderParameter.TIME,
                            ((DefaultParticleSystem) particleSystem).getParticleTime(i));
                }

                renderer.setColor(
                        renderBuffer.getColorR(renderOffset),
                        renderBuffer.getColorG(renderOffset),
                        renderBuffer.getColorB(renderOffset),
                        renderBuffer.getColorA(renderOffset));

                renderer.render(alpha);

                modelMatrix.popMatrix();
            }
        }
    }
}