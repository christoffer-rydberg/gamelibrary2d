package com.gamelibrary2d.particles;

import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.common.io.BufferUtils;
import com.gamelibrary2d.common.random.RandomInstance;
import com.gamelibrary2d.components.denotations.Clearable;
import com.gamelibrary2d.components.denotations.Updatable;
import com.gamelibrary2d.framework.OpenGL;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.glUtil.*;
import com.gamelibrary2d.particles.parameters.EmitterParameters;
import com.gamelibrary2d.particles.parameters.ParticleParameters;
import com.gamelibrary2d.particles.parameters.ParticleSystemParameters;
import com.gamelibrary2d.particles.parameters.PositionParameters;
import com.gamelibrary2d.particles.renderers.EfficientParticleRenderer;

import java.nio.FloatBuffer;

public class AcceleratedParticleSystem implements Updatable, Renderable, Clearable {
    private final static int WORK_GROUP_SIZE = 512;
    private final int glUniformDeltaTime;
    private final int glUniformParticleCount;
    private final ShaderProgram updateProgram;

    private final float[] position = new float[2];
    private final FloatBuffer externalAcceleration = BufferUtils.createFloatBuffer(2);

    private final MirroredIntBuffer atomicBuffer;
    private final MirroredFloatBuffer parametersBuffer;
    private final MirroredFloatBuffer positionBuffer;

    private final DefaultOpenGLBuffer[] updateBuffer;
    private final DefaultVertexArrayBuffer<DefaultOpenGLBuffer>[] renderBuffer;

    private final int capacity;

    private final int glUniformPosition;
    private final int glUniformExternalAcceleration;
    private final int glUniformParticlesInGpu;
    private final int glUniformRandomSeed;

    private Point positionTransformation;
    private int parameterUpdateCounter;
    private int positionUpdateCounter;
    private int particleCount;
    private int activeBuffer = 0;

    private EfficientParticleRenderer renderer;
    private ParticleSystemParameters parameters;

    private int particlesInGpuBuffer;

    private AcceleratedParticleSystem(ShaderProgram updateProgram,
                                      MirroredFloatBuffer positionBuffer,
                                      MirroredFloatBuffer parametersBuffer,
                                      DefaultOpenGLBuffer[] updateBuffer,
                                      DefaultVertexArrayBuffer<DefaultOpenGLBuffer>[] renderBuffer,
                                      ParticleSystemParameters parameters,
                                      EfficientParticleRenderer renderer,
                                      int capacity,
                                      Disposer disposer) {

        boolean updateProgramInUse = updateProgram.inUse();

        try {
            if (!updateProgramInUse) {
                updateProgram.bind();
            }

            this.updateProgram = updateProgram;

            // Cache uniforms
            glUniformDeltaTime = updateProgram.getUniformLocation("deltaTime");
            glUniformParticleCount = updateProgram.getUniformLocation("particleCount");

            this.updateBuffer = updateBuffer;
            this.renderBuffer = renderBuffer;
            this.parameters = parameters;
            this.renderer = renderer;
            this.capacity = capacity;
            atomicBuffer = MirroredIntBuffer.create(new int[1], OpenGL.GL_ATOMIC_COUNTER_BUFFER, OpenGL.GL_DYNAMIC_DRAW, disposer);

            this.positionBuffer = positionBuffer;
            positionUpdateCounter = parameters.getPositionParameters().getUpdateCounter();

            this.parametersBuffer = parametersBuffer;
            parameterUpdateCounter = parameters.getParticleParameters().getUpdateCounter();

            glUniformPosition = updateProgram.getUniformLocation("position");
            glUniformExternalAcceleration = updateProgram.getUniformLocation("externalAcceleration");
            glUniformParticlesInGpu = updateProgram.getUniformLocation("particlesInGpu");
            glUniformRandomSeed = updateProgram.getUniformLocation("randomSeed");

        } finally {
            if (!updateProgramInUse) {
                updateProgram.unbind();
            }
        }
    }

    public static AcceleratedParticleSystem create(
            ParticleSystemParameters parameters,
            EfficientParticleRenderer renderer,
            int capacity,
            Disposer disposer) {

        MirroredFloatBuffer positionBuffer = MirroredFloatBuffer.create(
                parameters.getPositionParameters().getInternalStateArray(),
                OpenGL.GL_SHADER_STORAGE_BUFFER,
                OpenGL.GL_DYNAMIC_DRAW,
                disposer);

        MirroredFloatBuffer parametersBuffer = MirroredFloatBuffer.create(
                parameters.getParticleParameters().getInternalStateArray(),
                OpenGL.GL_SHADER_STORAGE_BUFFER,
                OpenGL.GL_DYNAMIC_DRAW,
                disposer);

        DefaultVertexArrayBuffer[] renderBuffer = new DefaultVertexArrayBuffer[2];
        for (int i = 0; i < 2; ++i) {
            DefaultOpenGLBuffer buffer = DefaultOpenGLBuffer.create(
                    OpenGL.GL_ARRAY_BUFFER,
                    OpenGL.GL_DYNAMIC_DRAW,
                    disposer);

            buffer.allocate(capacity * ParticleRenderBuffer.STRIDE);

            renderBuffer[i] = new DefaultVertexArrayBuffer<>(
                    buffer,
                    ParticleRenderBuffer.STRIDE,
                    4);
        }

        DefaultOpenGLBuffer[] updateBuffer = new DefaultOpenGLBuffer[2];
        for (int i = 0; i < 2; ++i) {
            DefaultOpenGLBuffer buffer = DefaultOpenGLBuffer.create(
                    OpenGL.GL_SHADER_STORAGE_BUFFER,
                    OpenGL.GL_DYNAMIC_DRAW,
                    disposer);

            buffer.allocate(capacity * ParticleUpdateBuffer.STRIDE);

            updateBuffer[i] = buffer;
        }

        return new AcceleratedParticleSystem(ShaderProgram.getDefaultParticleUpdaterProgram(),
                positionBuffer, parametersBuffer, updateBuffer, renderBuffer, parameters, renderer, capacity, disposer);
    }

    public void setPosition(Point position) {
        setPosition(position.getX(), position.getY());
    }

    public void setPosition(float x, float y) {
        position[0] = x;
        position[1] = y;
    }

    public void setExternalAcceleration(float x, float y) {
        externalAcceleration.clear();
        externalAcceleration.put(x);
        externalAcceleration.put(y);
        externalAcceleration.flip();
    }

    public Point getPositionTransformation() {
        return positionTransformation;
    }

    public void setPositionTransformation(Point positionTransformation) {
        this.positionTransformation = positionTransformation;
    }

    /**
     * Emits all particles.
     * The number of particles is decided by the count-parameters of the particle system's {@link EmitterParameters}.
     */
    public void emit() {
        EmitterParameters emitterParameters = parameters.getEmitterParameters();
        emit(Math.round(emitterParameters.getParticleCount() + emitterParameters.getParticleCountVar() * RandomInstance.random11()));
    }

    /**
     * Emits particles.
     *
     * @param count The number of particles to emit.
     */
    public void emit(int count) {
        int remaining = capacity - particleCount;
        if (remaining < count) {
            count = remaining;
        }

        particleCount += count;
    }

    /**
     * Emits particles.
     * The number of particles is decided by the deltaTime parameter
     * in conjunction with the emission rate of the particle system's {@link EmitterParameters}.
     *
     * @param deltaTime The time, in seconds, since the last particle was emitted.
     * @return The time, in seconds, since the last particle was emitted.
     * If no particles were emitted, this will be the same as the deltaTime parameter.
     * This value should be added to the update cycle's deltaTime the next time this method is invoked.
     */
    public float emit(float deltaTime) {
        float rate = parameters.getEmitterParameters().getEmissionRate();
        if (rate > 0) {
            int numberOfEmissions = (int) (deltaTime * rate);
            for (int i = 0; i < numberOfEmissions; ++i) {
                emit();
            }

            return deltaTime - numberOfEmissions / rate;
        } else {
            return 0f; // No particles will ever be emitted
        }
    }

    @Override
    public void update(float deltaTime) {
        if (particleCount > 0) {
            // Reset atomic counter
            atomicBuffer.getData()[0] = 0;
            atomicBuffer.updateGPU(0, 1);

            OpenGL openGL = OpenGL.instance();

            updateProgram.bind();

            openGL.glUniform1f(glUniformDeltaTime, deltaTime);
            openGL.glUniform1i(glUniformParticleCount, particleCount);

            bindUpdateBuffers();

            openGL.glDispatchCompute((int) Math.ceil((double) particleCount / WORK_GROUP_SIZE), 1, 1);

            applyMemoryBarriers();

            updateProgram.unbind();

            // Update particle count
            atomicBuffer.updateCPU(0, 1);
            particlesInGpuBuffer = atomicBuffer.getData()[0];
            particleCount = particlesInGpuBuffer;

            activeBuffer = activeBuffer == 1 ? 0 : 1;
        }
    }

    private void bindUpdateBuffers() {
        OpenGL openGL = OpenGL.instance();

        openGL.glUniform1i(glUniformRandomSeed, RandomInstance.get().nextInt());

        openGL.glUniform1i(glUniformParticlesInGpu, particlesInGpuBuffer);

        PositionParameters positionParameters = parameters.getPositionParameters();

        openGL.glUniform2f(
                glUniformPosition,
                position[0] + positionParameters.getOffsetX(),
                position[1] + positionParameters.getOffsetY());
        openGL.glUniform2fv(glUniformExternalAcceleration, externalAcceleration);

        if (!positionBuffer.allocate(positionParameters.getInternalStateArray())) {
            if (positionUpdateCounter != positionParameters.getUpdateCounter()) {
                positionUpdateCounter = positionParameters.getUpdateCounter();
                positionBuffer.updateGPU(0, positionBuffer.getCapacity());
            }
        }

        ParticleParameters particleParameters = parameters.getParticleParameters();
        if (!parametersBuffer.allocate(particleParameters.getInternalStateArray())) {
            if (parameterUpdateCounter != particleParameters.getUpdateCounter()) {
                parameterUpdateCounter = particleParameters.getUpdateCounter();
                parametersBuffer.updateGPU(0, parametersBuffer.getCapacity());
            }
        }

        openGL.glBindBufferBase(OpenGL.GL_ATOMIC_COUNTER_BUFFER, 0, atomicBuffer.getBufferId());
        openGL.glBindBufferBase(OpenGL.GL_SHADER_STORAGE_BUFFER, 1, positionBuffer.getBufferId());
        openGL.glBindBufferBase(OpenGL.GL_SHADER_STORAGE_BUFFER, 2, parametersBuffer.getBufferId());
        openGL.glBindBufferBase(OpenGL.GL_SHADER_STORAGE_BUFFER, 3, renderBuffer[activeBuffer].getBufferId());
        openGL.glBindBufferBase(OpenGL.GL_SHADER_STORAGE_BUFFER, 4, updateBuffer[activeBuffer].getBufferId());
        openGL.glBindBufferBase(OpenGL.GL_SHADER_STORAGE_BUFFER, 5,
                renderBuffer[activeBuffer == 1 ? 0 : 1].getBufferId());
        openGL.glBindBufferBase(OpenGL.GL_SHADER_STORAGE_BUFFER, 6,
                updateBuffer[activeBuffer == 1 ? 0 : 1].getBufferId());
    }

    private boolean isTransformingPosition() {
        return positionTransformation != null && (positionTransformation.getX() != 0f || positionTransformation.getY() != 0f);
    }

    @Override
    public void render(float alpha) {
        if (particlesInGpuBuffer > 0) {
            if (isTransformingPosition()) {
                ModelMatrix.instance().pushMatrix();
                ModelMatrix.instance().translatef(positionTransformation.getX(), positionTransformation.getY(), 0);
                renderer.render(this, renderBuffer[activeBuffer], false, 0, particlesInGpuBuffer, alpha);
                ModelMatrix.instance().popMatrix();
            } else {
                renderer.render(this, renderBuffer[activeBuffer], false, 0, particlesInGpuBuffer, alpha);
            }
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

    public EfficientParticleRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(EfficientParticleRenderer renderer) {
        this.renderer = renderer;
    }

    public ParticleSystemParameters getParameters() {
        return parameters;
    }

    public void setParameters(ParticleSystemParameters parameters) {
        this.parameters = parameters;
    }

    private void applyMemoryBarriers() {
        OpenGL.instance().glMemoryBarrier(
                OpenGL.GL_SHADER_STORAGE_BARRIER_BIT |
                        OpenGL.GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);
    }
}