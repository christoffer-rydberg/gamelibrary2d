package com.gamelibrary2d.renderers;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.glUtil.ShaderParameter;
import com.gamelibrary2d.glUtil.ShaderProgram;
import com.gamelibrary2d.resources.Surface;
import com.gamelibrary2d.resources.Texture;

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
    protected void applyParameters(float alpha) {
        setShaderParameter(ShaderParameter.TEXTURED, texture != null ? 1 : 0);
        super.applyParameters(alpha);
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