package com.gamelibrary2d.demos.networkgame.client.objects.network.decoration;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.demos.networkgame.client.objects.network.AbstractClientObject;
import com.gamelibrary2d.demos.networkgame.client.objects.network.LocalPlayer;
import com.gamelibrary2d.demos.networkgame.client.objects.network.RemotePlayer;
import com.gamelibrary2d.demos.networkgame.common.GameSettings;
import com.gamelibrary2d.demos.networkgame.common.ObjectTypes;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.renderers.ContentRenderer;
import com.gamelibrary2d.renderers.SurfaceRenderer;
import com.gamelibrary2d.resources.Quad;
import com.gamelibrary2d.resources.Surface;
import com.gamelibrary2d.resources.Texture;

import java.util.HashMap;
import java.util.Map;

public class ContentMap {
    private final Map<Byte, Map<Byte, Renderable>> renderers = new HashMap<>();
    private PlayerRendererFactory playerRendererFactory;

    private void initializeRenderers(byte primaryType, TextureMap textures, Rectangle bounds, Disposer disposer) {
        Map<Byte, Renderable> renderers = new HashMap<>();
        Surface surface = Quad.create(bounds, disposer);
        for (Byte key : textures.getKeys(primaryType)) {
            ContentRenderer renderer = new SurfaceRenderer<>(surface, textures.getTexture(primaryType, key));
            renderers.put(key, renderer);
        }

        this.renderers.put(primaryType, renderers);
    }

    private void initializePlayerRenderers(GameSettings settings, TextureMap textures, Disposer disposer) {
        playerRendererFactory = new PlayerRendererFactory(
                textures.getPlayerBackground(),
                textures.getPlayerForeground(),
                Quad.create(settings.getSpaceCraftBounds().resize(1.25f), disposer));
    }

    private void initializeObstacleRenderers(GameSettings settings, TextureMap textures, Disposer disposer) {
        initializeRenderers(ObjectTypes.OBSTACLE, textures, settings.getObstacleBounds().resize(1.25f), disposer);
    }

    public void initialize(GameSettings settings, TextureMap textures, Disposer disposer) {
        initializePlayerRenderers(settings, textures, disposer);
        initializeObstacleRenderers(settings, textures, disposer);
    }

    public void setContent(LocalPlayer player) {
        Renderable content = playerRendererFactory.create(player.getColor());
        player.setContent(content);
    }

    public void setContent(RemotePlayer player) {
        Renderable content = playerRendererFactory.create(player.getColor());
        player.setContent(content);
    }

    public void setContent(AbstractClientObject obj) {
        obj.setContent(get(obj.getPrimaryType(), obj.getSecondaryType()));
    }

    private Renderable get(byte primaryType, byte secondaryType) {
        Map<Byte, Renderable> renderers = this.renderers.get(primaryType);
        return renderers != null ? renderers.get(secondaryType) : null;
    }

    private static class PlayerRendererFactory {
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
}
