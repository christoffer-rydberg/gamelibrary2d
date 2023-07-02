package com.gamelibrary2d.demos.networkgame.client.objects.network.decoration;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.demos.networkgame.client.objects.network.AbstractClientObject;
import com.gamelibrary2d.demos.networkgame.client.objects.network.LocalPlayer;
import com.gamelibrary2d.demos.networkgame.client.objects.network.RemotePlayer;
import com.gamelibrary2d.demos.networkgame.common.GameSettings;
import com.gamelibrary2d.demos.networkgame.common.ObjectTypes;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.Quad;
import com.gamelibrary2d.opengl.resources.Surface;
import com.gamelibrary2d.opengl.resources.Texture;

import java.util.Hashtable;


public class RendererMap {
    private final Hashtable<Byte, Hashtable<Byte, Renderable>> renderers = new Hashtable<>();
    private PlayerRendererFactory playerRendererFactory;

    private void initializeRenderers(byte primaryType, TextureMap textures, Rectangle bounds, Disposer disposer) {
        Hashtable<Byte, Renderable> renderers = new Hashtable<>();
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

    public void setRenderer(LocalPlayer player) {
        Renderable renderer = playerRendererFactory.create(player.getColor());
        player.setRenderer(renderer);
    }

    public void setRenderer(RemotePlayer player) {
        Renderable renderer = playerRendererFactory.create(player.getColor());
        player.setRenderer(renderer);
    }

    public void setRenderer(AbstractClientObject obj) {
        obj.setRenderer(get(obj.getPrimaryType(), obj.getSecondaryType()));
    }

    private Renderable get(byte primaryType, byte secondaryType) {
        Hashtable<Byte, Renderable> renderers = this.renderers.get(primaryType);
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
