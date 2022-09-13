package com.gamelibrary2d.opengl.renderers;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.components.denotations.Bounded;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.opengl.resources.DefaultFrameBuffer;
import com.gamelibrary2d.opengl.resources.FrameBuffer;
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

        FrameBuffer frameBuffer = DefaultFrameBuffer.create(
                (int) bounds.getWidth(),
                (int) bounds.getHeight(),
                disposer);

        this.frameBufferRenderer = new FrameBufferRenderer(bounds, frameBuffer);

        this.cacheRenderer = new SurfaceRenderer<>(
                Quad.create(bounds, disposer),
                frameBufferRenderer.getFrameBuffer().getTexture());
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
            frameBufferRenderer.render(renderer, 1f);
            cached = caching;
        }

        cacheRenderer.render(alpha);
    }

    public T getRenderer() {
        return renderer;
    }
}