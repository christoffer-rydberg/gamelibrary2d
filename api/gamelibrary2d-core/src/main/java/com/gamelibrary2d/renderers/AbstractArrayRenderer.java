package com.gamelibrary2d.renderers;

import com.gamelibrary2d.framework.OpenGL;
import com.gamelibrary2d.glUtil.*;
import com.gamelibrary2d.resources.BlendMode;

public abstract class AbstractArrayRenderer<T extends OpenGLBuffer> extends AbstractRenderer implements ArrayRenderer<T> {
    private boolean maskingOutBackground = false;
    private BlendMode blendMode = BlendMode.TRANSPARENT;

    protected AbstractArrayRenderer() {

    }

    protected AbstractArrayRenderer(float[] shaderParameters) {
        super(shaderParameters);
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
        ShaderProgram shaderProgram = getShaderProgram();
        shaderProgram.bind();
        shaderProgram.updateModelMatrix(ModelMatrix.instance());

        beforeRender(shaderProgram);

        array.bind();

        applyParameters(alpha);

        int drawMode = getOpenGlDrawMode();
        if (blendMode != BlendMode.NONE && isMaskingOutBackground()) {
            OpenGLUtils.setBlendMode(BlendMode.MASKED);
            OpenGL.instance().glDrawArrays(drawMode, offset, len);
        }

        OpenGLUtils.setBlendMode(blendMode);

        OpenGL.instance().glDrawArrays(drawMode, offset, len);

        array.unbind();
    }

    protected void applyParameters(float alpha) {
        float alphaSetting = getShaderParameter(ShaderParameter.ALPHA);

        try {
            ShaderProgram program = getShaderProgram();
            setShaderParameter(ShaderParameter.ALPHA, alphaSetting * alpha);
            applyShaderParameters(program);
            program.applyParameters();
        } finally {
            setShaderParameter(ShaderParameter.ALPHA, alphaSetting);
        }
    }

    protected abstract int getOpenGlDrawMode();

    protected abstract void beforeRender(ShaderProgram shaderProgram);

    protected abstract ShaderProgram getShaderProgram();
}