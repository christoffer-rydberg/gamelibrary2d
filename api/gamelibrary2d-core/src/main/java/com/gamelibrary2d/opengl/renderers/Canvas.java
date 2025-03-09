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

public class Canvas<T extends Renderable> implements Renderable, Bounded {
    private final T content;
    private final Rectangle bounds;
    private final SurfaceRenderer<Quad> canvasRenderer;
    private final FrameBufferRenderer frameBufferRenderer;
    private boolean refreshOnRender;
    private boolean refreshRequested = true;

    private Canvas(T content, Rectangle bounds, boolean refreshOnRender, Disposer disposer) {
        this.content = content;
        this.bounds = bounds;
        this.refreshOnRender = refreshOnRender;

        TextureFrameBuffer frameBuffer = TextureFrameBuffer.create(
                (int) Math.ceil(bounds.getWidth()),
                (int) Math.ceil(bounds.getHeight()),
                disposer);

        this.frameBufferRenderer = new FrameBufferRenderer(frameBuffer);

        this.canvasRenderer = new SurfaceRenderer<>(
                Quad.create(bounds, disposer),
                frameBuffer.getTexture());
    }

    public static <T extends Renderable> Canvas<T> create(T renderer, Rectangle bounds, boolean refreshOnRender, Disposer disposer) {
        return new Canvas<>(renderer, bounds, refreshOnRender, disposer);
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isRefreshingOnRender() {
        return refreshOnRender;
    }

    public void setRefreshOnRender(boolean refreshOnRender) {
        this.refreshOnRender = refreshOnRender;
    }

    public void refresh() {
        refreshRequested = true;
    }

    @Override
    public void render(float alpha) {
        if (refreshOnRender || refreshRequested) {
            refreshRequested = false;
            frameBufferRenderer.render(content, true, -bounds.getLowerX(), -bounds.getLowerY(), 1f);
        }

        canvasRenderer.render(alpha);
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

    public T getContent() {
        return content;
    }
}