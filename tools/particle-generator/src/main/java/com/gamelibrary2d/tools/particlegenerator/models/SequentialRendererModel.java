package com.gamelibrary2d.tools.particlegenerator.models;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.animations.*;
import com.gamelibrary2d.disposal.DefaultDisposer;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.io.Read;
import com.gamelibrary2d.opengl.renderers.BlendMode;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.Quad;
import com.gamelibrary2d.opengl.resources.QuadShape;
import com.gamelibrary2d.opengl.resources.Texture;
import com.gamelibrary2d.particles.SequentialParticleRenderer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SequentialRendererModel {
    private final SurfaceRenderer<Quad> surfaceRenderer;
    private final SequentialParticleRenderer renderer;
    private final Disposer resourceDisposer;

    private Rectangle bounds;
    private AnimationRenderer animationRenderer;
    private byte[] animationData;

    public SequentialRendererModel(Rectangle bounds, Disposer disposer) {
        this.resourceDisposer = new DefaultDisposer(disposer);
        this.surfaceRenderer = new SurfaceRenderer<>();
        this.renderer = new SequentialParticleRenderer(surfaceRenderer);
        setBounds(bounds);
    }

    void setBounds(Rectangle bounds) {
        resourceDisposer.dispose();
        this.bounds = bounds;
        if (surfaceRenderer.getTexture() != null) {
            surfaceRenderer.setSurface(
                    Quad.create(bounds, resourceDisposer));
        } else {
            surfaceRenderer.setSurface(
                    Quad.create(bounds, QuadShape.RADIAL_GRADIENT, resourceDisposer));
        }

        if (animationRenderer != null) {
            try {
                AnimationMetadata animationMetadata = AnimationLoader.load(
                        new ByteArrayInputStream(animationData),
                        StandardAnimationFormats.GIF);

                Animation animation = Animation.create(
                        animationMetadata,
                        bounds,
                        resourceDisposer);

                this.animationRenderer = new AnimationRenderer(animation, true, resourceDisposer);
                animationRenderer.setBlendMode(surfaceRenderer.getBlendMode());
                renderer.setRenderer(animationRenderer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setBlendMode(BlendMode blendMode) {
        surfaceRenderer.setBlendMode(blendMode);
        if (animationRenderer != null) {
            animationRenderer.setBlendMode(blendMode);
        }
    }

    public SequentialParticleRenderer getRenderer() {
        return renderer;
    }

    public void setTexture(Texture texture) {
        setBounds(bounds);
        surfaceRenderer.setTexture(texture);
        renderer.setRenderer(surfaceRenderer);
        animationRenderer = null;
    }

    public Animation setAnimation(URL url) throws IOException {
        resourceDisposer.dispose();
        surfaceRenderer.setTexture(null);

        try (InputStream stream = url.openStream()) {
            animationData = Read.byteArray(stream);
        }

        AnimationMetadata animationMetadata = AnimationLoader.load(
                new ByteArrayInputStream(animationData),
                StandardAnimationFormats.GIF);

        Animation animation = Animation.create(
                animationMetadata,
                bounds,
                resourceDisposer);

        this.animationRenderer = new AnimationRenderer(animation, true, resourceDisposer);
        animationRenderer.setBlendMode(surfaceRenderer.getBlendMode());
        renderer.setRenderer(animationRenderer);

        return animation;
    }
}