package com.gamelibrary2d.opengl.renderers;

import com.gamelibrary2d.opengl.resources.Surface;
import com.gamelibrary2d.opengl.resources.Texture;

public class SurfaceRenderer<T extends Surface> extends AbstractSurfaceRenderer<T> {

    public SurfaceRenderer() {
    }

    public SurfaceRenderer(float[] parameters) {
        super(parameters);
    }

    public SurfaceRenderer(T surface) {
        super(surface);
    }

    public SurfaceRenderer(T surface, Texture texture) {
        super(surface, texture);
    }

    @Override
    public Texture getTexture() {
        return super.getTexture();
    }

    @Override
    public void setTexture(Texture texture) {
        super.setTexture(texture);
    }

    @Override
    public T getSurface() {
        return super.getSurface();
    }

    @Override
    public void setSurface(T surface) {
        super.setSurface(surface);
    }
}