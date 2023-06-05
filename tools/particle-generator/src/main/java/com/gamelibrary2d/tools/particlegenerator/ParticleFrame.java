package com.gamelibrary2d.tools.particlegenerator;

import com.gamelibrary2d.input.Keyboard;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.Window;
import com.gamelibrary2d.components.containers.DefaultLayer;
import com.gamelibrary2d.components.containers.Layer;
import com.gamelibrary2d.components.denotations.KeyDownAware;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.tools.particlegenerator.models.ParticleSystemModel;
import com.gamelibrary2d.tools.particlegenerator.panels.EmitterPanel;
import com.gamelibrary2d.tools.particlegenerator.panels.ParticleSystemSettingsPanel;
import com.gamelibrary2d.tools.particlegenerator.panels.RenderingPanel;
import com.gamelibrary2d.tools.particlegenerator.panels.SaveLoadResetPanel;

public class ParticleFrame extends AbstractFrame implements KeyDownAware {
    private static final float WINDOW_MARGIN = 40;
    private final ParticleGenerator game;
    private int dragging = -1;
    private ParticleSystemModel particleSystem;
    private Layer<Renderable> screenLayer;
    private EmitterPanel emitterPanel;
    private boolean interfaceHidden = false;
    private float particleEmitterTime;

    ParticleFrame(ParticleGenerator game) {
        super(game);
        this.game = game;
    }

    @Override
    protected void onBegin() {
        screenLayer = new DefaultLayer<>();
        Layer<Renderable> particleLayer = new DefaultLayer<>();

        particleSystem = ParticleSystemModel.create(this);
        particleSystem.addToLayer(particleLayer);

        Layer<Renderable> backgroundLayer = new DefaultLayer<>();

        ParticleSystemSettingsPanel settingsPanel = new ParticleSystemSettingsPanel(particleSystem, this);
        settingsPanel.setPosition(180f, game.getWindow().getHeight() - 20f);

        Window window = game.getWindow();

        RenderingPanel renderingPanel = new RenderingPanel(particleSystem);
        renderingPanel.setPosition(
                window.getWidth() - renderingPanel.getBounds().getWidth() - WINDOW_MARGIN,
                window.getHeight() - WINDOW_MARGIN);

        emitterPanel = new EmitterPanel(particleSystem);
        emitterPanel.setPosition(
                window.getWidth() - WINDOW_MARGIN,
                emitterPanel.getBounds().getHeight() + WINDOW_MARGIN);

        SaveLoadResetPanel saveLoadResetPanel = new SaveLoadResetPanel(particleSystem, game);
        saveLoadResetPanel.setPosition(WINDOW_MARGIN, WINDOW_MARGIN);

        particleSystem.setPosition(
                game.getWindow().getWidth() / 2f,
                game.getWindow().getHeight() / 2f
        );
        add(backgroundLayer);
        add(particleLayer);
        add(screenLayer);
        screenLayer.add(settingsPanel);
        screenLayer.add(renderingPanel);
        screenLayer.add(emitterPanel);
        screenLayer.add(saveLoadResetPanel);
    }

    @Override
    protected void onEnd() {

    }

    @Override
    protected void onDispose() {

    }

    @Override
    protected void onUpdate(float deltaTime) {
        super.onUpdate(deltaTime);
        if (emitterPanel.isLaunchingSequential()) {
            particleEmitterTime = particleSystem.emit(particleEmitterTime + deltaTime);
        }
    }

    @Override
    protected boolean onPointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {
        if (!super.onPointerDown(id, button, x, y, transformedX, transformedY)) {
            if (dragging == -1) {
                dragging = id;
                particleSystem.setPosition(transformedX, transformedY);
                return true;
            }

            return false;
        }

        return true;
    }

    @Override
    protected boolean onPointerMove(int id, float x, float y, float transformedX, float transformedY) {
        if (!super.onPointerMove(id, x, y, transformedX, transformedY)) {
            if (dragging != -1) {
                particleSystem.setPosition(transformedX, transformedY);
                return true;
            }

            return false;
        }

        return true;
    }

    @Override
    protected void onPointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {
        if (id == dragging) {
            dragging = -1;
        }
        super.onPointerUp(id, button, x, y, transformedX, transformedY);
    }

    @Override
    public void keyDown(int key, boolean repeat) {
        if (!repeat && key == Keyboard.instance().keyEscape()) {
            if (!interfaceHidden) {
                remove(screenLayer);
                interfaceHidden = true;
            } else {
                add(screenLayer);
                interfaceHidden = false;
            }
        }
    }
}