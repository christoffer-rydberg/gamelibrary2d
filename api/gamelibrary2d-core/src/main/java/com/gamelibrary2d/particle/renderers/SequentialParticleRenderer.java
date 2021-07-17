package com.gamelibrary2d.particle.renderers;

import com.gamelibrary2d.glUtil.ModelMatrix;
import com.gamelibrary2d.glUtil.OpenGLBuffer;
import com.gamelibrary2d.glUtil.ShaderParameter;
import com.gamelibrary2d.particle.systems.DefaultParticleSystem;
import com.gamelibrary2d.particle.systems.ParticleRenderBuffer;
import com.gamelibrary2d.renderers.ContentRenderer;

public class SequentialParticleRenderer implements ParticleRenderer {

    private final DefaultParticleSystem particleSystem;
    private ContentRenderer renderer;

    public SequentialParticleRenderer(DefaultParticleSystem particleSystem, ContentRenderer renderer) {
        this.particleSystem = particleSystem;
        this.renderer = renderer;
    }

    public ContentRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(ContentRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void render(OpenGLBuffer buffer, boolean gpuOutdated, int offset, int len, float alpha) {
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

                renderer.setShaderParameter(
                        ShaderParameter.TIME,
                        particleSystem.getParticleTime(i));

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