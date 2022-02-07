package com.gamelibrary2d.renderers;

import com.gamelibrary2d.glUtil.OpenGLUtils;
import com.gamelibrary2d.glUtil.ShaderProgram;
import com.gamelibrary2d.resources.BlendMode;

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
        OpenGLUtils.setBlendMode(blendMode);
        onRender(shaderProgram);
    }

    protected abstract void onRender(ShaderProgram shaderProgram);
}
