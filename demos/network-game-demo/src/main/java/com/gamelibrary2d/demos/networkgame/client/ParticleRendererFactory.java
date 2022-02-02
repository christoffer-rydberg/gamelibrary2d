package com.gamelibrary2d.demos.networkgame.client;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.framework.OpenGL;
import com.gamelibrary2d.particles.renderers.EfficientParticleRenderer;
import com.gamelibrary2d.particles.renderers.ParticleRenderer;
import com.gamelibrary2d.particles.renderers.SequentialParticleRenderer;
import com.gamelibrary2d.renderers.SurfaceRenderer;
import com.gamelibrary2d.resources.BlendMode;
import com.gamelibrary2d.resources.Quad;
import com.gamelibrary2d.resources.QuadShape;
import com.gamelibrary2d.resources.Texture;

public class ParticleRendererFactory {
    public static ParticleRenderer create(Disposer disposer) {
        switch (OpenGL.instance().getSupportedVersion()) {
            case OPENGL_ES_3:
                Quad quad = Quad.create(Rectangle.create(16f, 16f), QuadShape.RADIAL_GRADIENT, disposer);
                SurfaceRenderer renderer = new SurfaceRenderer<>(quad);
                renderer.setBlendMode(BlendMode.ADDITIVE);
                return new SequentialParticleRenderer(renderer);
            case OPENGL_ES_3_1:
            case OPENGL_ES_3_2:
            case OPENGL_CORE_430:
            default:
                return new EfficientParticleRenderer();
        }
    }

    public static ParticleRenderer create(Texture texture, BlendMode blendMode, Disposer disposer) {
        switch (OpenGL.instance().getSupportedVersion()) {
            case OPENGL_ES_3: {
                Quad quad = Quad.create(Rectangle.create(16f, 16f), QuadShape.RADIAL_GRADIENT, disposer);
                SurfaceRenderer renderer = new SurfaceRenderer<>(quad, texture);
                renderer.setBlendMode(blendMode);
                return new SequentialParticleRenderer(renderer);
            }
            case OPENGL_ES_3_1:
            case OPENGL_ES_3_2:
            case OPENGL_CORE_430:
            default: {
                EfficientParticleRenderer renderer = new EfficientParticleRenderer();
                renderer.setTexture(texture);
                renderer.setBlendMode(blendMode);
                return renderer;
            }
        }
    }
}
