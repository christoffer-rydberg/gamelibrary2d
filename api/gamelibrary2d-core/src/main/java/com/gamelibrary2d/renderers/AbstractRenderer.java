package com.gamelibrary2d.renderers;

import com.gamelibrary2d.glUtil.ModelMatrix;
import com.gamelibrary2d.glUtil.ShaderProgram;
import com.gamelibrary2d.glUtil.ShaderParameter;

public class AbstractRenderer implements Renderer {
    private ShaderProgram shaderProgram;
    private final float[] shaderParameters;

    protected AbstractRenderer() {
        this(ShaderProgram.getDefaultShaderProgram());
    }

    protected AbstractRenderer(float[] shaderParameters) {
        this(ShaderProgram.getDefaultShaderProgram(), shaderParameters);
    }

    protected AbstractRenderer(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
        shaderParameters = new float[ShaderParameter.MIN_PARAMETERS];
        shaderParameters[ShaderParameter.COLOR_R] = 1;
        shaderParameters[ShaderParameter.COLOR_G] = 1;
        shaderParameters[ShaderParameter.COLOR_B] = 1;
        shaderParameters[ShaderParameter.ALPHA] = 1;
    }

    protected AbstractRenderer(ShaderProgram shaderProgram, float[] shaderParameters) {
        this.shaderProgram = shaderProgram;
        this.shaderParameters = shaderParameters;
    }

    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }

    public void setShaderProgram(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
    }

    protected ShaderProgram prepareShaderProgram(float alpha) {
        float alphaSetting = getShaderParameter(ShaderParameter.ALPHA);

        shaderProgram.bind();
        shaderProgram.updateModelMatrix(ModelMatrix.instance());

        try {
            setShaderParameter(ShaderParameter.ALPHA, alphaSetting * alpha);
            shaderProgram.setParameters(shaderParameters, 0, shaderParameters.length);
        } finally {
            setShaderParameter(ShaderParameter.ALPHA, alphaSetting);
        }

        shaderProgram.applyParameters();

        return shaderProgram;
    }

    @Override
    public float getShaderParameter(int setting) {
        return shaderParameters[setting];
    }

    @Override
    public void setShaderParameter(int setting, float value) {
        shaderParameters[setting] = value;
    }
}
