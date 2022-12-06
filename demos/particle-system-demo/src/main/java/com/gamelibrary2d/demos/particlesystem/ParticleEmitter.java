package com.gamelibrary2d.demos.particlesystem;

import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.particles.DefaultParticleSystem;

public class ParticleEmitter implements Updatable {
    private final float posX;
    private final float posY;
    private final DefaultParticleSystem particleSystem;

    private float timer;

    public ParticleEmitter(float posX, float posY, DefaultParticleSystem particleSystem) {
        this.posX = posX;
        this.posY = posY;
        this.particleSystem = particleSystem;
    }

    @Override
    public void update(float deltaTime) {
        timer = particleSystem.emit(posX, posY, timer + deltaTime);
    }
}
