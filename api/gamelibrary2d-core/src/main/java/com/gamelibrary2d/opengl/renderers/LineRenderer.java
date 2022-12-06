package com.gamelibrary2d.opengl.renderers;

import com.gamelibrary2d.OpenGL;
import com.gamelibrary2d.opengl.buffers.PositionBuffer;
import com.gamelibrary2d.opengl.shaders.ShaderProgram;

public class LineRenderer extends AbstractArrayRenderer<PositionBuffer> {
    private boolean loop;
    private float lineWidth;

    public LineRenderer() {
        this(1f);
    }

    public LineRenderer(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    @Override
    protected int getOpenGlDrawMode() {
        return isLoop() ? OpenGL.GL_LINE_LOOP : OpenGL.GL_LINE_STRIP;
    }

    @Override
    protected void renderPrepare(ShaderProgram shaderProgram) {
        OpenGL.instance().glLineWidth(lineWidth);
    }
}
