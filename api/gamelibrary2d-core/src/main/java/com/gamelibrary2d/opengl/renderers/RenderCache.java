package com.gamelibrary2d.opengl.renderers;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.denotations.Bounded;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.opengl.OpenGLState;
import com.gamelibrary2d.opengl.resources.FrameBuffer;
import com.gamelibrary2d.opengl.resources.PixelParser;
import com.gamelibrary2d.opengl.resources.TextureFrameBuffer;
import com.gamelibrary2d.opengl.resources.Quad;

public class RenderCache<T extends Renderable> implements Renderable, Bounded {
    private final T renderer;
    private final Rectangle bounds;
    private final SurfaceRenderer<Quad> cacheRenderer;
    private final FrameBufferRenderer frameBufferRenderer;
    private boolean caching;
    private boolean cached;

    private RenderCache(T renderer, Rectangle bounds, Disposer disposer) {
        this.renderer = renderer;
        this.bounds = bounds;

        TextureFrameBuffer frameBuffer = TextureFrameBuffer.create(
                (int) Math.ceil(bounds.getWidth()),
                (int) Math.ceil(bounds.getHeight()),
                disposer);

        this.frameBufferRenderer = new FrameBufferRenderer(frameBuffer);

        this.cacheRenderer = new SurfaceRenderer<>(
                Quad.create(bounds, disposer),
                frameBuffer.getTexture());
    }

    public static <T extends Renderable> RenderCache<T> create(T renderer, Rectangle bounds, Disposer disposer) {
        return new RenderCache<>(renderer, bounds, disposer);
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    public void flushCache() {
        cached = false;
    }

    public boolean isCaching() {
        return caching;
    }

    public void setCaching(boolean caching) {
        this.caching = caching;
    }

    @Override
    public void render(float alpha) {
        if (!cached) {
            frameBufferRenderer.render(renderer, true, -bounds.getLowerX(), -bounds.getLowerY(), 1f);
            cached = caching;
        }

        cacheRenderer.render(alpha);
    }

    public boolean isPixelVisible(float x, float y, int alphaThreshold) {
        if (!bounds.contains(x, y)) {
            return false;
        }

        FrameBuffer frameBuffer = frameBufferRenderer.getFrameBuffer();
        int previousFbo = frameBuffer.bind();
        try {
            int pixel = frameBuffer.getPixel((int) (x - bounds.getLowerX()), (int) (y - bounds.getLowerY()));
            int alpha = PixelParser.getA(pixel);
            return alpha > alphaThreshold;
        } finally {
            OpenGLState.bindFrameBuffer(previousFbo);
        }
    }

    public T getRenderer() {
        return renderer;
    }
}