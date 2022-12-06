package com.gamelibrary2d.particles;

import com.gamelibrary2d.OpenGL;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.io.BufferUtils;
import com.gamelibrary2d.opengl.OpenGLState;
import com.gamelibrary2d.opengl.buffers.MirroredBuffer;
import com.gamelibrary2d.opengl.buffers.OpenGLBuffer;
import com.gamelibrary2d.opengl.renderers.AbstractArrayRenderer;
import com.gamelibrary2d.opengl.renderers.BlendMode;
import com.gamelibrary2d.opengl.renderers.PointSmoothing;
import com.gamelibrary2d.opengl.resources.Texture;
import com.gamelibrary2d.opengl.shaders.ShaderParameter;
import com.gamelibrary2d.opengl.shaders.ShaderProgram;

import java.nio.FloatBuffer;

public class EfficientParticleRenderer extends AbstractArrayRenderer<OpenGLBuffer> implements ParticleRenderer {
    private final static String boundsUniformName = "bounds";
    private final FloatBuffer boundsBuffer = BufferUtils.createFloatBuffer(4);

    private float pointSize = 1f;
    private ParticleShape particleShape;
    private PointSmoothing pointSmoothing = PointSmoothing.FASTEST;
    private Texture texture;
    private Rectangle bounds;

    public EfficientParticleRenderer() {
        setBlendMode(BlendMode.ADDITIVE);
        setBounds(Rectangle.create(16f, 16f));
        setParticleShape(ParticleShape.QUAD);
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

    public PointSmoothing getPointSmoothing() {
        return pointSmoothing;
    }

    public void setPointSmoothing(PointSmoothing pointSmoothing) {
        this.pointSmoothing = pointSmoothing;
    }

    public ParticleShape getParticleShape() {
        return particleShape;
    }

    public void setParticleShape(ParticleShape particleShape) {
        switch (particleShape) {
            case POINT:
                this.particleShape = particleShape;
                setShaderProgram(OpenGLState.getPointParticleShaderProgram());
                break;
            case QUAD:
                this.particleShape = particleShape;
                setShaderProgram(OpenGLState.getQuadParticleShaderProgram());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + particleShape);
        }
    }

    public float getPointSize() {
        return pointSize;
    }

    public void setPointSize(float pointSize) {
        this.pointSize = pointSize;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    @Override
    public void render(Object particleSystem, OpenGLBuffer buffer, boolean gpuOutdated, int offset, int len, float alpha) {
        if (gpuOutdated && buffer instanceof MirroredBuffer) {
            buffer.bind();
            ((MirroredBuffer) buffer).updateGPU(offset, len);
        }

        super.render(alpha, buffer, offset, len);
    }

    @Override
    protected void renderPrepare(ShaderProgram shaderProgram) {
        if (particleShape == ParticleShape.QUAD) {
            int glBoundsUniform = shaderProgram.getUniformLocation(boundsUniformName);
            OpenGL.instance().glUniform4fv(glBoundsUniform, boundsBuffer);

            Texture texture = getTexture();
            if (texture != null) {
                texture.bind();
                setShaderParameter(ShaderParameter.TEXTURED, 1);
            } else {
                setShaderParameter(ShaderParameter.TEXTURED, 0);
            }
        } else {
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

    @Override
    protected int getOpenGlDrawMode() {
        return OpenGL.GL_POINTS;
    }
}
