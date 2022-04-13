package com.gamelibrary2d.opengl.renderers;

import com.gamelibrary2d.opengl.OpenGLState;
import com.gamelibrary2d.opengl.shaders.ShaderProgram;

public abstract class AbstractContentRenderer extends AbstractRenderer implements ContentRenderer {
    private BlendMode blendMode = BlendMode.TRANSPARENT;

    protected AbstractContentRenderer() {

    }

    protected AbstractContentRenderer(float[] shaderParameters) {
        super(shaderParameters);
    }

    protected AbstractContentRenderer(ShaderProgram shaderProgram) {
        super(shaderProgram);
    }

    protected AbstractContentRenderer(ShaderProgram shaderProgram, float[] shaderParameters) {
        super(shaderProgram, shaderParameters);
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
    }

    @Override
    public void render(float alpha) {
        ShaderProgram shaderProgram = prepareShaderProgram(alpha);
        OpenGLState.setBlendMode(blendMode);
        onRender(shaderProgram);
    }

    protected abstract void onRender(ShaderProgram shaderProgram);
}
