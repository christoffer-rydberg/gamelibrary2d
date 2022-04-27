package com.gamelibrary2d.demos.particlesystem;

import com.gamelibrary2d.Game;
import com.gamelibrary2d.common.io.ResourceReader;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.components.frames.FrameInitializationContext;
import com.gamelibrary2d.components.frames.FrameInitializer;
import com.gamelibrary2d.particles.*;
import com.gamelibrary2d.updaters.DurationUpdater;
import com.gamelibrary2d.updaters.InstantUpdater;
import com.gamelibrary2d.updaters.SequentialUpdater;
import com.gamelibrary2d.updates.EmptyUpdate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DemoFrame extends AbstractFrame {
    private final Game game;
    private final List<ParticleEmitter> emitters = new ArrayList<>();
    private DefaultParticleSystem fireSystem;
    private DefaultParticleSystem explosionSystem;

    DemoFrame(Game game) {
        super(game);
        this.game = game;
    }

    private ParticleSpawnParameters createSpawnParameters() {
        ParticleSpawnParameters spawnParameters = new ParticleSpawnParameters();
        spawnParameters.setOffsetXVar(75f);
        spawnParameters.setOffsetYVar(25f);
        return spawnParameters;
    }

    private ParticleUpdateParameters createUpdateParameters() {
        ParticleUpdateParameters updateSettings = new ParticleUpdateParameters();
        updateSettings.setLife(1.5f);
        updateSettings.setLifeVar(0.5f);
        updateSettings.setDelay(0.25f);
        updateSettings.setDelayVar(0.25f);
        updateSettings.setSpeed(250f);
        updateSettings.setSpeedVar(100f);
        updateSettings.setAccelerationX(150f);
        updateSettings.setAccelerationXVar(100f);
        updateSettings.setScale(2.5f);
        updateSettings.setScaleVar(2.4f);
        updateSettings.setColor(175f, 50f, 30f);
        updateSettings.setUpdateColor(true);
        updateSettings.setEndColor(127.5f, 127.5f, 127.5f);
        updateSettings.setAlpha(0.75f);
        updateSettings.setAlphaVar(0.25f);
        updateSettings.setUpdateAlpha(true);
        return updateSettings;
    }

    @Override
    protected void onInitialize(FrameInitializer initializer) {
        try {
            // Example of particle system settings created from code:
            ParticleEmissionParameters emissionParameters = new ParticleEmissionParameters();
            emissionParameters.setEmissionRate(350f);

            ParticleSystemParameters fireSystemSettings = new ParticleSystemParameters(
                    emissionParameters,
                    createSpawnParameters(),
                    createUpdateParameters());

            fireSystem = DefaultParticleSystem.create(fireSystemSettings, this);

            // Example of particle system settings loaded from file:
            ParticleSystemParameters explosionSystemSettings = new ResourceReader().read(
                    getClass().getResource("/explosion.particle"),
                    ParticleSystemParameters::new);

            explosionSystem = DefaultParticleSystem.create(explosionSystemSettings, this);

            add(fireSystem);
            add(explosionSystem);
        } catch (IOException e) {
            e.printStackTrace();
            game.exit();
        }
    }

    @Override
    protected void onInitialized(FrameInitializationContext context, Throwable error) {

    }

    @Override
    protected void onBegin() {

    }

    @Override
    protected void onEnd() {

    }

    @Override
    protected void onDispose() {

    }

    private void createFire(float posX, float posY, float delay) {
        SequentialUpdater updater = new SequentialUpdater();
        updater.add(new DurationUpdater(delay, new EmptyUpdate()));
        updater.add(new InstantUpdater(dt -> emitters.add(new ParticleEmitter(posX, posY, fireSystem))));
        startUpdater(updater);
    }

    @Override
    protected boolean onPointerDown(int id, int button, float x, float y, float projectedX, float projectedY) {
        explosionSystem.emit(projectedX, projectedY);
        createFire(projectedX, projectedY, 1f);
        return true;
    }

    @Override
    public void onUpdate(float deltaTime) {
        for (ParticleEmitter emitter : emitters) {
            emitter.update(deltaTime);
        }
        super.onUpdate(deltaTime);
    }
}