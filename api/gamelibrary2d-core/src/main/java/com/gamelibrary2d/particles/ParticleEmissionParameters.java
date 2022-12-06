package com.gamelibrary2d.particles;

import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.io.Serializable;

public class ParticleEmissionParameters implements Serializable {
    private int particleCount;
    private int particleCountVar;
    private float emissionRate;

    public ParticleEmissionParameters() {
        particleCount = 1;
        emissionRate = 1;
    }

    public ParticleEmissionParameters(DataBuffer buffer) {
        particleCount = buffer.getInt();
        particleCountVar = buffer.getInt();
        emissionRate = buffer.getFloat();
    }

    /**
     * The number of particles per emission.
     */
    public int getParticleCount() {
        return particleCount;
    }

    /**
     * Sets the {@link #getParticleCount particle count}.
     */
    public void setParticleCount(int particleCount) {
        this.particleCount = particleCount;
    }

    /**
     * Variance for the {@link #getParticleCount particle count}.
     */
    public int getParticleCountVar() {
        return particleCountVar;
    }

    /**
     * Sets the {@link #getParticleCountVar particle count variance}.
     */
    public void setParticleCountVar(int particleCountVar) {
        this.particleCountVar = particleCountVar;
    }

    /**
     * @return The number of emissions per second when continuously emitting particles.
     */
    public float getEmissionRate() {
        return emissionRate;
    }

    /**
     * Sets the {@link #getEmissionRate emission rate}.
     */
    public void setEmissionRate(float emissionRate) {
        this.emissionRate = emissionRate;
    }

    @Override
    public void serialize(DataBuffer buffer) {
        buffer.putInt(particleCount);
        buffer.putInt(particleCountVar);
        buffer.putFloat(emissionRate);
    }
}
