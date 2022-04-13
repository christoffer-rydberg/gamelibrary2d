package com.gamelibrary2d.particles;

import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.common.random.RandomInstance;
import com.gamelibrary2d.components.denotations.Clearable;
import com.gamelibrary2d.components.denotations.Updatable;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.opengl.ModelMatrix;

public class DefaultParticleSystem implements Updatable, Renderable, Clearable {
    private final float[] externalSpeed = new float[2];
    private final float[] externalAcceleration = new float[2];
    private final ParticleRenderBuffer renderBuffer;
    private final ParticleUpdateBuffer updateBuffer;
    private final Particle particle;

    private int particleCount;
    private boolean gpuOutdated = true;
    private ParticleSystemParameters parameters;
    private ParticleRenderer renderer;
    private Point positionTransformation;
    private ParticleUpdateListener updateListener;

    private DefaultParticleSystem(
            ParticleSystemParameters parameters,
            ParticleRenderer renderer,
            ParticleRenderBuffer renderBuffer,
            ParticleUpdateBuffer updateBuffer) {

        this.parameters = parameters;
        this.renderer = renderer;
        this.renderBuffer = renderBuffer;
        this.updateBuffer = updateBuffer;
        this.particle = new Particle(renderBuffer, updateBuffer, 0);
    }

    public static DefaultParticleSystem create(ParticleSystemParameters parameters, Disposer disposer) {
        return create(parameters, new EfficientParticleRenderer(), disposer);
    }

    public static DefaultParticleSystem create(
            ParticleSystemParameters parameters,
            ParticleRenderer renderer,
            Disposer disposer) {
        return create(parameters, renderer, parameters.estimateCapacity(), disposer);
    }

    public static DefaultParticleSystem create(
            ParticleSystemParameters parameters,
            int initialCapacity,
            Disposer disposer) {
        return create(parameters, new EfficientParticleRenderer(), initialCapacity, disposer);
    }

    public static DefaultParticleSystem create(
            ParticleSystemParameters parameters,
            ParticleRenderer renderer,
            int initialCapacity,
            Disposer disposer) {

        return new DefaultParticleSystem(
                parameters,
                renderer,
                ParticleRenderBuffer.create(initialCapacity, disposer),
                ParticleUpdateBuffer.create(initialCapacity, disposer));
    }

    public float getParticleTime(int index) {
        return updateBuffer.getTime(index * updateBuffer.getStride());
    }

    public void setExternalSpeed(float x, float y) {
        externalSpeed[0] = x;
        externalSpeed[1] = y;
    }

    public void setExternalAcceleration(float x, float y) {
        externalAcceleration[0] = x;
        externalAcceleration[1] = y;
    }

    public void setSettings(ParticleSystemParameters parameters) {
        this.parameters = parameters;
    }

    public ParticleSystemParameters getParameters() {
        return parameters;
    }

    public ParticleRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(ParticleRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Gets the update listener.
     *
     * @return The update listener, or null if no update listener has been set.
     */
    public ParticleUpdateListener getUpdateListener() {
        return updateListener;
    }

    /**
     * Sets the update listener.
     *
     * @param listener The update listener.
     */
    public void setUpdateListener(ParticleUpdateListener listener) {
        updateListener = listener;
    }

    public Point getPositionTransformation() {
        return positionTransformation;
    }

    public void setPositionTransformation(Point positionTransformation) {
        this.positionTransformation = positionTransformation;
    }

    /**
     * Emits particles at the specified position.
     *
     * @param x     The X-position of the particles.
     * @param y     The Y-position of the particles.
     * @param count The number of particles to emit.
     */
    public void emit(float x, float y, int count) {
        int updatedParticleCount = particleCount + count;
        renderBuffer.ensureCapacity(updatedParticleCount * renderBuffer.getStride());
        updateBuffer.ensureCapacity(updatedParticleCount * updateBuffer.getStride());
        for (int i = 0; i < count; ++i) {
            onEmit(x, y);
        }
    }

    /**
     * Emits particles at the specified position.
     *
     * @param position The position of the particles.
     * @param count    The number of particles to emit.
     */
    public void emit(Point position, int count) {
        emit(position.getX(), position.getY(), count);
    }

    /**
     * Emits particles at the specified position.
     * The number of particles is decided by the count-parameters of the particle system's {@link ParticleEmissionParameters}.
     *
     * @param x The X-position of the particles.
     * @param y The Y-position of the particles.
     */
    public void emit(float x, float y) {
        ParticleEmissionParameters emissionParameters = parameters.getEmissionParameters();
        int count = Math.round(emissionParameters.getParticleCount() + emissionParameters.getParticleCountVar() * RandomInstance.random11());
        emit(x, y, count);
    }

    public void emit(Point position) {
        emit(position.getX(), position.getY());
    }

    /**
     * Emits particles at the specified position.
     * The number of particles is decided by the deltaTime parameter
     * in conjunction with the emission rate of the particle system's {@link ParticleEmissionParameters}.
     *
     * @param x         The X-position of the emitted particles.
     * @param y         The Y-position of the emitted particles.
     * @param deltaTime The time, in seconds, since the last particle was emitted.
     * @return The time, in seconds, since the last particle was emitted.
     * If no particles were emitted, this will be the same as the deltaTime parameter.
     * This value should be added to the update cycle's deltaTime the next time this method is invoked.
     */
    public float emit(float x, float y, float deltaTime) {
        float rate = parameters.getEmissionParameters().getEmissionRate();
        if (rate > 0) {
            int numberOfEmissions = (int) (deltaTime * rate);
            for (int i = 0; i < numberOfEmissions; ++i) {
                emit(x, y);
            }

            return deltaTime - numberOfEmissions / rate;
        } else {
            return 0f; // No particles will ever be emitted
        }
    }

    public float emit(Point position, float deltaTime) {
        return emit(position.getX(), position.getY(), deltaTime);
    }

    private void onEmit(float x, float y) {
        particle.setIndex(particleCount++);

        particle.setInitialized(false);
        double spawnAngle = parameters.getSpawnParameters().apply(particle, x, y);
        parameters.getUpdateParameters().apply(particle, x, y, spawnAngle);

        particle.setExternalSpeedX(externalSpeed[0]);
        particle.setExternalSpeedY(externalSpeed[1]);
    }

    private boolean isTransformingPosition() {
        return positionTransformation != null && (positionTransformation.getX() != 0f || positionTransformation.getY() != 0f);
    }

    public void update(float deltaTime) {
        if (particleCount > 0) {
            int index = 0;
            while (index != particleCount) {
                particle.setIndex(index);
                particle.update(externalAcceleration, deltaTime);

                if (particle.hasExpired()) {
                    destroyParticle(index);
                    continue;
                }

                if (updateListener != null) {
                    final boolean compensateForPosTransform = isTransformingPosition();
                    if (compensateForPosTransform) {
                        particle.setPosition(particle.getPosX() + positionTransformation.getX(),
                                particle.getPosY() + positionTransformation.getY());
                    }

                    if (!updateListener.updated(this, particle)) {
                        destroyParticle(index);
                        continue;
                    }

                    if (compensateForPosTransform) {
                        particle.setPosition(particle.getPosX() - positionTransformation.getX(),
                                particle.getPosY() - positionTransformation.getY());
                    }
                }

                ++index;
            }

            gpuOutdated = true;
        }
    }

    public void render(float alpha) {
        if (particleCount > 0) {
            if (isTransformingPosition()) {
                ModelMatrix.instance().pushMatrix();
                ModelMatrix.instance().translatef(positionTransformation.getX(), positionTransformation.getY(), 0);
                renderer.render(this, renderBuffer, gpuOutdated, 0, particleCount, alpha);
                ModelMatrix.instance().popMatrix();
            } else {
                renderer.render(this, renderBuffer, gpuOutdated, 0, particleCount, alpha);
            }

            gpuOutdated = false;
        }
    }

    @Override
    public void clear() {
        particleCount = 0;
    }

    @Override
    public boolean isAutoClearing() {
        return true;
    }

    public int getParticleCount() {
        return particleCount;
    }

    private void destroyParticle(int index) {
        --particleCount;

        if (particleCount > 0) {
            int lastIndex = particleCount;
            renderBuffer.copy(lastIndex, index, 1);
            updateBuffer.copy(lastIndex, index, 1);
        }
    }

    public interface ParticleUpdateListener {

        /**
         * @param system   The particle system owning the particle.
         * @param particle The updated particle.
         * @return True if the particle has collided and should be destroyed.
         */
        boolean updated(DefaultParticleSystem system, Particle particle);
    }
}