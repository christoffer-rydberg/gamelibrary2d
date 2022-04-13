package com.gamelibrary2d.text;

import com.gamelibrary2d.opengl.shaders.ShaderProgram;

public interface Font {

    int getAscent();

    int getDescent();

    int getHeight();

    float getTextWidth(String text, int offset, int length);

    void render(ShaderProgram shaderProgram, String text, int offset, int length);
}
