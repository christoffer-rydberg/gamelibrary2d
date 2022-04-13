package com.gamelibrary2d.opengl.shaders;

public interface ShaderProgram {

    void bind();

    void unbind();

    float getParameter(int index);

    boolean setParameter(int index, float value);

    void setParameters(float[] params, int offset, int length);

    void applyParameters();

    int getUniformLocation(CharSequence name);

    int getAttributeLocation(CharSequence name);

    void updateModelMatrix();
}
