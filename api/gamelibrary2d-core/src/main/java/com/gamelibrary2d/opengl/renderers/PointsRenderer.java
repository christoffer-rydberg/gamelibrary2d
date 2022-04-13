package com.gamelibrary2d.opengl.renderers;

import com.gamelibrary2d.framework.OpenGL;
import com.gamelibrary2d.opengl.buffers.PositionBuffer;
import com.gamelibrary2d.opengl.shaders.ShaderProgram;

public class PointsRenderer extends AbstractArrayRenderer<PositionBuffer> {
    private float pointSize = 1f;
    private PointSmoothing pointSmoothing = PointSmoothing.FASTEST;

    public PointsRenderer() {

    }

    public PointsRenderer(float pointSize) {
        this.pointSize = pointSize;
    }

    public PointsRenderer(float pointSize, PointSmoothing pointSmoothing) {
        this.pointSize = pointSize;
        this.pointSmoothing = pointSmoothing;
    }

    public PointSmoothing getPointSmoothing() {
        return pointSmoothing;
    }

    public void setPointSmoothing(PointSmoothing pointSmoothing) {
        this.pointSmoothing = pointSmoothing;
    }

    public float getPointSize() {
        return pointSize;
    }

    public void setPointSize(float pointSize) {
        this.pointSize = pointSize;
    }

    @Override
    protected int getOpenGlDrawMode() {
        return OpenGL.GL_POINTS;
    }

    @Override
    protected void renderPrepare(ShaderProgram shaderProgram) {
        OpenGL.instance().glPointSize(pointSize);
        switch (pointSmoothing) {
            case FASTEST:
                OpenGL.instance().glEnable(OpenGL.GL_POINT_SMOOTH);
                OpenGL.instance().glHint(OpenGL.GL_POINT_SMOOTH_HINT, OpenGL.GL_FASTEST);
                break;
            case NICEST:
                OpenGL.instance().glEnable(OpenGL.GL_POINT_SMOOTH);
                OpenGL.instance().glHint(OpenGL.GL_POINT_SMOOTH_HINT, OpenGL.GL_NICEST);
                break;
            case NONE:
                OpenGL.instance().glDisable(OpenGL.GL_POINT_SMOOTH);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + pointSmoothing);
        }
    }
}
