package com.gamelibrary2d.renderers;

import com.gamelibrary2d.glUtil.ModelMatrix;
import com.gamelibrary2d.glUtil.OpenGLUtils;
import com.gamelibrary2d.glUtil.ShaderParameter;
import com.gamelibrary2d.glUtil.ShaderProgram;
import com.gamelibrary2d.resources.BlendMode;

public abstract class AbstractContentRenderer extends AbstractRenderer implements ContentRenderer {
    private BlendMode blendMode = BlendMode.TRANSPARENT;
    private ShaderProgram shaderProgram;

    protected AbstractContentRenderer() {
        shaderProgram = ShaderProgram.getDefaultShaderProgram();
    }

    protected AbstractContentRenderer(float[] shaderParameters) {
        super(shaderParameters);
        shaderProgram = ShaderProgram.getDefaultShaderProgram();
    }

    public ShaderProgram getShaderProgram() {
        return shaderProgram != null ? shaderProgram : ShaderProgram.getDefaultShaderProgram();
    }

    public void setShaderProgram(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
    }

    @Override
    public void render(float alpha) {
        ShaderProgram shaderProgram = getShaderProgram();
        shaderProgram.bind();
        shaderProgram.updateModelMatrix(ModelMatrix.instance());
        applyParameters(alpha);
        OpenGLUtils.setBlendMode(blendMode);
        onRender(shaderProgram);
    }

    protected void applyParameters(float alpha) {
        float alphaSetting = getShaderParameter(ShaderParameter.ALPHA);

        try {
            setShaderParameter(ShaderParameter.ALPHA, alphaSetting * alpha);
            applyShaderParameters(shaderProgram);
            shaderProgram.applyParameters();
        } finally {
            setShaderParameter(ShaderParameter.ALPHA, alphaSetting);
        }
    }

    protected abstract void onRender(ShaderProgram shaderProgram);
}
