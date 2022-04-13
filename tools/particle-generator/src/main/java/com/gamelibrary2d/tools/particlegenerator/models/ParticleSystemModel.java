package com.gamelibrary2d.tools.particlegenerator.models;

import com.gamelibrary2d.animations.Animation;
import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.disposal.DefaultDisposer;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.components.containers.Layer;
import com.gamelibrary2d.components.frames.Frame;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.opengl.renderers.BlendMode;
import com.gamelibrary2d.opengl.resources.DefaultTexture;
import com.gamelibrary2d.opengl.resources.Texture;
import com.gamelibrary2d.particles.*;

import java.io.IOException;
import java.net.URL;

public class ParticleSystemModel {
    private final Frame frame;
    private final DefaultParticleSystem defaultParticleSystem;
    private final AcceleratedParticleSystem acceleratedParticleSystem;

    private final EfficientParticleRenderer efficientRenderer;
    private final SequentialRendererModel sequentialRenderer;

    private final Disposer textureDisposer;

    private final Point pos = new Point();
    private BlendMode blendMode;
    private ParticleSystemParameters settings;
    private ParticleSystemType particleSystemType = ParticleSystemType.EFFICIENT;
    private Rectangle bounds;

    private ParticleSystemModel(Frame frame) {
        this.frame = frame;
        textureDisposer = new DefaultDisposer(frame);

        efficientRenderer = new EfficientParticleRenderer();

        settings = new ParticleSystemParameters(
                new ParticleEmissionParameters(),
                new ParticleSpawnParameters(),
                new ParticleUpdateParameters());

        defaultParticleSystem = DefaultParticleSystem.create(settings, efficientRenderer, frame);
        acceleratedParticleSystem = AcceleratedParticleSystem.create(settings, efficientRenderer, 10000000, frame);

        sequentialRenderer = new SequentialRendererModel(efficientRenderer.getBounds(), frame);

        updateParticleSize(
                efficientRenderer.getBounds().getWidth(),
                efficientRenderer.getBounds().getHeight());

        setBlendMode(BlendMode.ADDITIVE);
    }

    public static ParticleSystemModel create(Frame frame) {
        return new ParticleSystemModel(frame);
    }

    public void setPosition(float x, float y) {
        pos.set(x, y);
    }

    public Point getPosition() {
        return pos;
    }

    public void reset() {
        setSettings(new ParticleSystemParameters(
                new ParticleEmissionParameters(),
                new ParticleSpawnParameters(),
                new ParticleUpdateParameters()));
    }

    public void removeTexture() {
        efficientRenderer.setTexture(null);
        sequentialRenderer.setTexture(null);
    }

    public void loadTexture(URL url) throws IOException {
        textureDisposer.dispose();
        if (url.getPath().endsWith(".gif")) {
            Animation animation = sequentialRenderer.setAnimation(url);
            efficientRenderer.setTexture(
                    animation.getFrame(0).getTexture()
            );
        } else {
            Texture texture = DefaultTexture.create(url, textureDisposer);
            sequentialRenderer.setTexture(texture);
            efficientRenderer.setTexture(texture);
        }
    }

    public EfficientParticleRenderer getEfficientRenderer() {
        return efficientRenderer;
    }

    public ParticleSystemType getParticleSystemType() {
        return particleSystemType;
    }

    public void setParticleSystemType(ParticleSystemType particleSystemType) {
        frame.invokeLater(() -> {
            this.particleSystemType = particleSystemType;
            switch (particleSystemType) {
                case SEQUENTIAL:
                    defaultParticleSystem.setRenderer(sequentialRenderer.getRenderer());
                    break;
                case EFFICIENT:
                    defaultParticleSystem.setRenderer(efficientRenderer);
                    break;
            }
        });
    }

    public ParticleSystemParameters getParameters() {
        return settings;
    }

    public void setParameters(ParticleUpdateParameters updateSettings) {
        settings.setUpdateParameters(updateSettings);
    }

    public ParticleSpawnParameters getSpawnParameters() {
        return settings.getSpawnParameters();
    }

    public void setSpawnParameters(ParticleSpawnParameters spawnParameters) {
        settings.setSpawnParameters(spawnParameters);
    }

    public float emit(float deltaTime) {
        switch (particleSystemType) {
            case ACCELERATED:
                acceleratedParticleSystem.setPosition(pos);
                return acceleratedParticleSystem.emit(deltaTime);
            case EFFICIENT:
            case SEQUENTIAL:
                return defaultParticleSystem.emit(pos, deltaTime);
            default:
                throw new IllegalStateException("Unexpected value: " + particleSystemType);
        }
    }

    public void emit() {
        switch (particleSystemType) {
            case ACCELERATED:
                acceleratedParticleSystem.setPosition(pos);
                acceleratedParticleSystem.emit();
                break;
            case EFFICIENT:
            case SEQUENTIAL:
                defaultParticleSystem.emit(pos);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + particleSystemType);
        }
    }

    public void emitAll() {
        switch (particleSystemType) {
            case ACCELERATED:
                acceleratedParticleSystem.setPosition(pos);
                acceleratedParticleSystem.emit();
                break;
            case EFFICIENT:
            case SEQUENTIAL:
                defaultParticleSystem.emit(pos);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + particleSystemType);
        }
    }

    public ParticleSystemParameters getSettings() {
        return settings;
    }

    public void setSettings(ParticleSystemParameters settings) {
        this.settings = settings;
        defaultParticleSystem.setSettings(settings);
        acceleratedParticleSystem.setParameters(settings);
    }

    public void addToLayer(Layer<Renderable> layer) {
        layer.add(defaultParticleSystem);
        layer.add(acceleratedParticleSystem);
    }

    public float getWidth() {
        return bounds.getWidth();
    }

    public void setWidth(float width) {
        if (getWidth() != width) {
            updateParticleSize(width, getHeight());
        }
    }

    public float getHeight() {
        return bounds.getHeight();
    }

    public void setHeight(float height) {
        if (getHeight() != height) {
            updateParticleSize(getWidth(), height);
        }
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    public void setBlendMode(BlendMode blendMode) {
        if (this.blendMode != blendMode) {
            this.blendMode = blendMode;
            efficientRenderer.setBlendMode(blendMode);
            sequentialRenderer.setBlendMode(blendMode);
        }
    }

    private void updateParticleSize(float width, float height) {
        bounds = Rectangle.create(width, height);
        efficientRenderer.setBounds(bounds);
        sequentialRenderer.setBounds(bounds);
    }

    public enum ParticleSystemType {
        SEQUENTIAL,
        EFFICIENT,
        ACCELERATED
    }
}