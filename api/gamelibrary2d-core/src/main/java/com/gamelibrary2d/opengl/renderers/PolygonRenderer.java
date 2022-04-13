package com.gamelibrary2d.opengl.renderers;

import com.gamelibrary2d.framework.OpenGL;
import com.gamelibrary2d.opengl.buffers.PositionBuffer;
import com.gamelibrary2d.opengl.shaders.ShaderProgram;

public class PolygonRenderer extends AbstractArrayRenderer<PositionBuffer> {

    @Override
    protected int getOpenGlDrawMode() {
        return OpenGL.GL_POLYGON;
    }

    @Override
    protected void renderPrepare(ShaderProgram shaderProgram) {

    }
}
