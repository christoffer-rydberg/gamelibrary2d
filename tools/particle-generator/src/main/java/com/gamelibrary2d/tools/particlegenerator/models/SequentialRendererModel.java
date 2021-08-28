package com.gamelibrary2d.tools.particlegenerator.models;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.disposal.DefaultDisposer;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.common.io.Read;
import com.gamelibrary2d.imaging.AnimationLoader;
import com.gamelibrary2d.imaging.AnimationMetadata;
import com.gamelibrary2d.imaging.StandardAnimationFormats;
import com.gamelibrary2d.particle.renderers.SequentialParticleRenderer;
import com.gamelibrary2d.renderers.AnimationRenderer;
import com.gamelibrary2d.renderers.SurfaceRenderer;
import com.gamelibrary2d.resources.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SequentialRendererModel {
    private final SurfaceRenderer surfaceRenderer;
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