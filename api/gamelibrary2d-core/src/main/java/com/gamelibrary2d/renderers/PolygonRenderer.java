package com.gamelibrary2d.renderers;

import com.gamelibrary2d.framework.OpenGL;
import com.gamelibrary2d.glUtil.PositionBuffer;
import com.gamelibrary2d.glUtil.ShaderProgram;

public class PolygonRenderer extends AbstractArrayRenderer<PositionBuffer> {

    @Override
    protected int getOpenGlDrawMode() {
        return OpenGL.GL_POLYGON;
    }

    @Override
    protected void beforeRender(ShaderProgram shaderProgram) {

    }

    @Override
    protected ShaderProgram getShaderProgram() {
        return ShaderProgram.getPointShaderProgram();
    }
}
