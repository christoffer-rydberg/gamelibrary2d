package com.gamelibrary2d.opengl.renderers;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.opengl.resources.Surface;
import com.gamelibrary2d.opengl.resources.Texture;
import com.gamelibrary2d.opengl.shaders.ShaderParameter;
import com.gamelibrary2d.opengl.shaders.ShaderProgram;

abstract class AbstractSurfaceRenderer<T extends Surface> extends AbstractContentRenderer {
    private T surface;
    private Texture texture;

    protected AbstractSurfaceRenderer(float[] shaderParameters) {
        super(shaderParameters);
    }

    protected AbstractSurfaceRenderer() {
        this(null, null);
    }

    protected AbstractSurfaceRenderer(T surface) {
        this(surface, null);
    }

    protected AbstractSurfaceRenderer(T surface, Texture texture) {
        setSurface(surface);
        setTexture(texture);
    }

    public T getSurface() {
        return surface;
    }

    void setSurface(T surface) {
        this.surface = surface;
    }

    public Texture getTexture() {
        return texture;
    }

    void setTexture(Texture texture) {
        this.texture = texture;
    }

    @Override
    public Rectangle getBounds() {
        return surface == null ? Rectangle.EMPTY : surface.getBounds();
    }

    @Override
    protected ShaderProgram prepareShaderProgram(float alpha) {
        setShaderParameter(ShaderParameter.TEXTURED, texture != null ? 1 : 0);
        return super.prepareShaderProgram(alpha);
    }

    @Override
    public void onRender(ShaderProgram shaderProgram) {
        if (surface != null) {
            if (texture != null) {
                texture.bind();
            }
            surface.render(shaderProgram);
        }
    }
}