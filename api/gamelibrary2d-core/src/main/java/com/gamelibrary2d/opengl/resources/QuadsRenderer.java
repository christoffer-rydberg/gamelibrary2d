package com.gamelibrary2d.opengl.resources;

import com.gamelibrary2d.OpenGL;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.io.BufferUtils;
import com.gamelibrary2d.opengl.OpenGLState;
import com.gamelibrary2d.opengl.buffers.PositionBuffer;
import com.gamelibrary2d.opengl.renderers.AbstractArrayRenderer;
import com.gamelibrary2d.opengl.shaders.ShaderProgram;

import java.nio.FloatBuffer;

public class QuadsRenderer extends AbstractArrayRenderer<PositionBuffer> {
    private final static String boundsUniformName = "bounds";
    private final static String texturedUniformName = "textured";
    private final static String shapeUniformName = "shape";
    private final FloatBuffer boundsBuffer = BufferUtils.createFloatBuffer(4);

    private Texture texture;
    private Rectangle bounds;
    private QuadShape shape = QuadShape.RECTANGLE;

    public QuadsRenderer(Rectangle bounds) {
        super(OpenGLState.getPointShaderProgram());
        setBounds(bounds);
    }

    public QuadsRenderer(Rectangle bounds, Texture texture) {
        this(bounds);
        this.texture = texture;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
        boundsBuffer.clear();
        boundsBuffer.put(bounds.getLowerX());
        boundsBuffer.put(bounds.getLowerY());
        boundsBuffer.put(bounds.getUpperX());
        boundsBuffer.put(bounds.getUpperY());
        boundsBuffer.flip();
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public QuadShape getShape() {
        return shape;
    }

    public void setShape(QuadShape shape) {
        this.shape = shape;
    }

    @Override
    protected void renderPrepare(ShaderProgram shaderProgram) {
        if (texture != null) {
            texture.bind();
        }

        int glBoundsUniform = shaderProgram.getUniformLocation(boundsUniformName);
        OpenGL.instance().glUniform4fv(glBoundsUniform, boundsBuffer);

        int glTexturedUniform = shaderProgram.getUniformLocation(texturedUniformName);
        OpenGL.instance().glUniform1i(glTexturedUniform, texture != null ? 1 : 0);

        int glShapeUniform = shaderProgram.getUniformLocation(shapeUniformName);
        switch (shape) {
            case RECTANGLE:
                OpenGL.instance().glUniform1i(glShapeUniform, 0);
                break;
            case RADIAL_GRADIENT:
                OpenGL.instance().glUniform1i(glShapeUniform, 1);
                break;
        }
    }

    @Override
    protected int getOpenGlDrawMode() {
        return OpenGL.GL_POINTS;
    }

}
