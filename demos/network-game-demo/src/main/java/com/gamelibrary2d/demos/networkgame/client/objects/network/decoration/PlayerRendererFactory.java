package com.gamelibrary2d.demos.networkgame.client.objects.network.decoration;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.renderers.SurfaceRenderer;
import com.gamelibrary2d.resources.Quad;
import com.gamelibrary2d.resources.Texture;

public class PlayerRendererFactory {
    private final Texture background;
    private final Texture foreground;
    private final Quad quad;

    public PlayerRendererFactory(Texture background, Texture foreground, Quad quad) {
        this.background = background;
        this.foreground = foreground;
        this.quad = quad;
    }

    public Renderable create(Color color) {
        SurfaceRenderer<Quad> backgroundRenderer = new SurfaceRenderer<>(quad, background);
        backgroundRenderer.setColor(color);
        SurfaceRenderer<Quad> foregroundRenderer = new SurfaceRenderer<>(quad, foreground);
        return a -> {
            backgroundRenderer.render(a);
            foregroundRenderer.render(a);
        };
    }
}
