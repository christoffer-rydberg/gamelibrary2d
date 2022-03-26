package com.gamelibrary2d.particles.parameters;

import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.io.Serializable;

public final class ParticleSystemParameters implements Serializable {
    private ParticleEmissionParameters emissionParameters;
    private ParticleSpawnParameters spawnParameters;
    private ParticleUpdateParameters updateParameters;

    public ParticleSystemParameters(
            ParticleEmissionParameters emissionParameters,
            ParticleSpawnParameters spawnParameters,
            ParticleUpdateParameters updateParameters) {
        this.emissionParameters = emissionParameters;
        this.spawnParameters = spawnParameters;
        this.updateParameters = updateParameters;
    }

    public ParticleSystemParameters(DataBuffer buffer) {
        this.updateParameters = new ParticleUpdateParameters(buffer);
        this.spawnParameters = new ParticleSpawnParameters(buffer);
        this.emissionParameters = new ParticleEmissionParameters(buffer);
    }

    public int estimateCapacityFromCount() {
        ParticleEmissionParameters emissionParameters = getEmissionParameters();
        return emissionParameters.getParticleCount() + emissionParameters.getParticleCountVar();
    }

    public int estimateCapacityFromInterval() {
        float maxLife = getUpdateParameters().getLife() + getUpdateParameters().getLifeVar();

        ParticleEmissionParameters emissionParameters = getEmissionParameters();

        int particleCount = emissionParameters.getParticleCount() + emissionParameters.getParticleCountVar();

        return (int) Math.ceil(1.2f * maxLife * particleCount / emissionParameters.getEmissionRate());
    }

    public int estimateCapacity() {
        return Math.max(
                estimateCapacityFromCount(),
                estimateCapacityFromInterval());
    }

    public ParticleEmissionParameters getEmissionParameters() {
        return emissionParameters;
    }

    public void setEmissionParameters(ParticleEmissionParameters emissionParameters) {
        this.emissionParameters = emissionParameters;
    }

    public ParticleSpawnParameters getSpawnParameters() {
        return spawnParameters;
    }

    public void setSpawnParameters(ParticleSpawnParameters spawnParameters) {
        this.spawnParameters = spawnParameters;
    }

    public ParticleUpdateParameters getUpdateParameters() {
        return updateParameters;
    }

    public void setUpdateParameters(ParticleUpdateParameters updateParameters) {
        this.updateParameters = updateParameters;
    }

    @Override
    public void serialize(DataBuffer buffer) {
        updateParameters.serialize(buffer);
        spawnParameters.serialize(buffer);
        emissionParameters.serialize(buffer);
    }
}