package com.gamelibrary2d.particles;

import com.gamelibrary2d.OpenGL;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.opengl.buffers.AbstractMirroredVertexArrayBuffer;
import com.gamelibrary2d.opengl.buffers.MirroredBuffer;
import com.gamelibrary2d.opengl.buffers.MirroredFloatBuffer;
import com.gamelibrary2d.opengl.buffers.OpenGLBuffer;
import com.gamelibrary2d.opengl.shaders.ShaderProgram;

public class CustomParticleSystem implements Updatable, Renderable {
    private final static int WORK_GROUP_SIZE = 512;

    private final int glUniformDeltaTime;
    private final int glUniformParticleCount;
    private final ShaderProgram updateProgram;

    private final MirroredFloatBuffer updateBuffer;
    private final OpenGLBuffer renderBuffer;
    private final EfficientParticleRenderer renderer;

    private CustomParticleSystem(
            ShaderProgram updateProgram,
            MirroredFloatBuffer updateBuffer,
            OpenGLBuffer renderBuffer,
            EfficientParticleRenderer renderer) {

        updateProgram.bind();

        this.updateProgram = updateProgram;

        // Cache uniforms
        glUniformDeltaTime = updateProgram.getUniformLocation("deltaTime");
        glUniformParticleCount = updateProgram.getUniformLocation("particleCount");

        this.updateBuffer = updateBuffer;
        this.renderBuffer = renderBuffer;
        this.renderer = renderer;
    }

    public static CustomParticleSystem create(
            float[] state,
            float[] update,
            int stride,
            ShaderProgram updateProgram,
            EfficientParticleRenderer renderer,
            Disposer disposer) {

        MirroredFloatBuffer updateBuffer = MirroredFloatBuffer.create(
                update, OpenGL.GL_SHADER_STORAGE_BUFFER, OpenGL.GL_DYNAMIC_DRAW, disposer);

        MirroredFloatBuffer renderBuffer = MirroredFloatBuffer.create(
                state, OpenGL.GL_ARRAY_BUFFER, OpenGL.GL_DYNAMIC_DRAW, disposer);

        CustomParticleBuffer arrayBuffer = CustomParticleBuffer.create(
                renderBuffer,
                stride,
                4,
                disposer);

        updateBuffer.updateGPU(0, updateBuffer.getCapacity());
        arrayBuffer.updateGPU(0, arrayBuffer.getCapacity());

        return new CustomParticleSystem(updateProgram, updateBuffer, arrayBuffer, renderer);
    }

    protected void bindUpdateBuffers() {
        OpenGL openGL = OpenGL.instance();
        openGL.glBindBufferBase(OpenGL.GL_SHADER_STORAGE_BUFFER, 0, renderBuffer.getBufferId());
        openGL.glBindBufferBase(OpenGL.GL_SHADER_STORAGE_BUFFER, 1, updateBuffer.getBufferId());
    }

    @Override
    public void render(float alpha) {
        renderer.render(this, renderBuffer, false, 0, getParticleCount(), alpha);
    }

    public int getParticleCount() {
        return renderBuffer.getCapacity();
    }

    @Override
    public void update(float deltaTime) {
        OpenGL openGL = OpenGL.instance();

        int particleCount = getParticleCount();

        updateProgram.bind();

        openGL.glUniform1f(glUniformDeltaTime, deltaTime);
        openGL.glUniform1i(glUniformParticleCount, particleCount);

        bindUpdateBuffers();

        openGL.glDispatchCompute((int) Math.ceil((double) particleCount / WORK_GROUP_SIZE), 1, 1);

        applyMemoryBarriers();

        updateProgram.unbind();
    }

    private void applyMemoryBarriers() {
        OpenGL.instance().glMemoryBarrier(
                OpenGL.GL_SHADER_STORAGE_BARRIER_BIT |
                        OpenGL.GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);
    }

    private static class CustomParticleBuffer extends AbstractMirroredVertexArrayBuffer<MirroredBuffer> {
        private CustomParticleBuffer(MirroredBuffer buffer, int stride, int elementSize) {
            super(buffer, stride, elementSize);
        }

        public static CustomParticleBuffer create(MirroredBuffer buffer, int stride, int elementSize, Disposer disposer) {
            CustomParticleBuffer obj = new CustomParticleBuffer(buffer, stride, elementSize);
            disposer.registerDisposal(obj);
            return obj;
        }
    }
}