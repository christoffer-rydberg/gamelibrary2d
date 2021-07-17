package com.gamelibrary2d.renderers;

import com.gamelibrary2d.framework.OpenGL;
import com.gamelibrary2d.glUtil.*;
import com.gamelibrary2d.resources.BlendMode;

public abstract class AbstractArrayRenderer<T extends OpenGLBuffer> extends AbstractRenderer implements ArrayRenderer<T> {
    private final DrawMode drawMode;
    private boolean maskingOutBackground = false;
    private BlendMode blendMode = BlendMode.TRANSPARENT;

    protected AbstractArrayRenderer(DrawMode drawMode, float[] shaderParameters) {
        super(shaderParameters);
        this.drawMode = drawMode;
    }

    protected AbstractArrayRenderer(DrawMode drawMode) {
        this.drawMode = drawMode;
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

    private int getOpenGlDrawMode() {
        switch (drawMode) {
            case POINTS:
                return OpenGL.GL_POINTS;
            case LINE:
                return OpenGL.GL_LINE_STRIP;
            default:
                throw new IllegalStateException("Unexpected value: " + drawMode);
        }
    }

    @Override
    public void render(float alpha, OpenGLBuffer array, int offset, int len) {
        ShaderProgram shaderProgram = getShaderProgram();
        shaderProgram.bind();
        shaderProgram.updateModelMatrix(ModelMatrix.instance());

        renderPrepare(shaderProgram);

        array.bind();

        applyParameters(alpha);

        int drawMode = getOpenGlDrawMode();
        if (blendMode != BlendMode.NONE && isMaskingOutBackground()) {
            OpenGLUtils.setBlendMode(BlendMode.MASKED);
            OpenGL.instance().glDrawArrays(drawMode, offset, len);
        }

        OpenGLUtils.setBlendMode(blendMode);

        OpenGL.instance().glDrawArrays(drawMode, offset, len);

        // Cleanup
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

    protected abstract ShaderProgram getShaderProgram();

    protected abstract void renderPrepare(ShaderProgram shaderProgram);

    protected enum DrawMode {
        POINTS,
        LINE
    }
}