package com.gamelibrary2d.opengl.renderers;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.opengl.shaders.ShaderParameter;

public interface Renderer {

    float getShaderParameter(int index);

    void setShaderParameter(int index, float value);

    default void setColor(Color color) {
        setColor(color, color.getA());
    }

    default void setColor(Color color, float alpha) {
        setColor(color.getR(), color.getG(), color.getB(), alpha);
    }

    default void setColor(float r, float g, float b) {
        setShaderParameter(ShaderParameter.COLOR_R, r);
        setShaderParameter(ShaderParameter.COLOR_G, g);
        setShaderParameter(ShaderParameter.COLOR_B, b);
    }

    default void setColor(float r, float g, float b, float a) {
        setShaderParameter(ShaderParameter.COLOR_R, r);
        setShaderParameter(ShaderParameter.COLOR_G, g);
        setShaderParameter(ShaderParameter.COLOR_B, b);
        setShaderParameter(ShaderParameter.ALPHA, a);
    }
}
