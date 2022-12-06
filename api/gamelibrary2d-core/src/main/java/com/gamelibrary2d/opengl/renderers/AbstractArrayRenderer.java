package com.gamelibrary2d.opengl.renderers;

import com.gamelibrary2d.OpenGL;
import com.gamelibrary2d.opengl.OpenGLState;
import com.gamelibrary2d.opengl.buffers.OpenGLBuffer;
import com.gamelibrary2d.opengl.shaders.ShaderProgram;

public abstract class AbstractArrayRenderer<T extends OpenGLBuffer> extends AbstractRenderer implements ArrayRenderer<T> {
    private BlendMode blendMode = BlendMode.TRANSPARENT;
    private boolean maskingOutBackground = false;

    protected AbstractArrayRenderer() {
        super(OpenGLState.getPointShaderProgram());
    }

    protected AbstractArrayRenderer(float[] shaderParameters) {
        super(OpenGLState.getPointShaderProgram(), shaderParameters);
    }

    protected AbstractArrayRenderer(ShaderProgram shaderProgram) {
        super(shaderProgram);
    }

    protected AbstractArrayRenderer(ShaderProgram shaderProgram, float[] shaderParameters) {
        super(shaderProgram, shaderParameters);
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
    }

    public boolean isMaskingOutBackground() {
        return maskingOutBackground;
    }

    /**
     * Setting this property to true will ensure that the color of the background
     * does not get blended into the rendered vertices. This is achieved by masking
     * out the background, which requires an extra render operation.
     *
     * @param maskingOutBackground True to mask out the background, false otherwise.
     */
    public void setMaskingOutBackground(boolean maskingOutBackground) {
        this.maskingOutBackground = maskingOutBackground;
    }

    @Override
    public void render(float alpha, OpenGLBuffer array, int offset, int len) {
        ShaderProgram shaderProgram = prepareShaderProgram(alpha);

        array.bind();
        renderPrepare(shaderProgram);

        BlendMode blendMode = getBlendMode();
        int drawMode = getOpenGlDrawMode();
        if (blendMode != BlendMode.NONE && isMaskingOutBackground()) {
            OpenGLState.setBlendMode(BlendMode.MASKED);
            OpenGL.instance().glDrawArrays(drawMode, offset, len);
        }

        OpenGLState.setBlendMode(blendMode);
        OpenGL.instance().glDrawArrays(drawMode, offset, len);

        array.unbind();
    }

    protected abstract int getOpenGlDrawMode();

    protected abstract void renderPrepare(ShaderProgram shaderProgram);
}