package com.gamelibrary2d.renderers;

import com.gamelibrary2d.glUtil.ShaderProgram;
import com.gamelibrary2d.glUtil.ShaderParameter;

public class AbstractRenderer implements Renderer {
    private final float[] shaderParameters;

    protected AbstractRenderer() {
        shaderParameters = new float[ShaderParameter.MIN_PARAMETERS];
        shaderParameters[ShaderParameter.COLOR_R] = 1;
        shaderParameters[ShaderParameter.COLOR_G] = 1;
        shaderParameters[ShaderParameter.COLOR_B] = 1;
        shaderParameters[ShaderParameter.ALPHA] = 1;
    }

    protected AbstractRenderer(float[] shaderParameters) {
        this.shaderParameters = shaderParameters;
    }

    protected void applyShaderParameters(ShaderProgram program) {
        program.setParameters(shaderParameters, 0, shaderParameters.length);
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
