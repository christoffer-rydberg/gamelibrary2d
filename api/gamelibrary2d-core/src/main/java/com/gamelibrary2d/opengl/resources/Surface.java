package com.gamelibrary2d.opengl.resources;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.disposal.Disposable;
import com.gamelibrary2d.opengl.shaders.ShaderProgram;

public interface Surface extends Disposable {

    Rectangle getBounds();

    void render(ShaderProgram shaderProgram);
}