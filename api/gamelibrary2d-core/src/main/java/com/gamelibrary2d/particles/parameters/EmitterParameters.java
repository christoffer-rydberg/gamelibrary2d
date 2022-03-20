package com.gamelibrary2d.particles.parameters;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.io.Serializable;

/**
 * Parameters for particle emission.
 */
public class EmitterParameters implements Serializable {
    private int particleCount;
    private int particleCountVar;
    private float emissionRate;

    public EmitterParameters() {
        particleCount = 1;
        emissionRate = 1;
    }

    public EmitterParameters(DataBuffer buffer) {
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
