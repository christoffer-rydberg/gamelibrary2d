package com.gamelibrary2d.demos.lightning;

import com.gamelibrary2d.Game;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.io.ResourceReader;
import com.gamelibrary2d.components.containers.DefaultLayerObject;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.components.frames.FrameInitializationContext;
import com.gamelibrary2d.components.frames.FrameInitializer;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.framework.Window;
import com.gamelibrary2d.lightning.*;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.DefaultTexture;
import com.gamelibrary2d.opengl.resources.Quad;
import com.gamelibrary2d.opengl.resources.Surface;
import com.gamelibrary2d.opengl.resources.Texture;
import com.gamelibrary2d.particles.DefaultParticleSystem;
import com.gamelibrary2d.particles.ParticleSystemParameters;

import java.io.IOException;
import java.net.URL;

class DemoFrame extends AbstractFrame {

    private final Game game;

    DemoFrame(Game game) {
        super(game);
        this.game = game;
    }

    private URL getUrl(String resource) {
        return getClass().getClassLoader().getResource(resource);
    }

    private ContentRenderer createRenderer(Rectangle bounds, String image) throws IOException {
        URL backgroundUrl = getUrl(image);
        Texture backgroundTexture = DefaultTexture.create(backgroundUrl, this);
        Surface backgroundQuad = Quad.create(bounds, this);
        return new SurfaceRenderer<>(backgroundQuad, backgroundTexture);
    }

    private Renderable createBackground(Window window) throws IOException {
        Rectangle bounds = new Rectangle(0, 0, window.getWidth(), window.getHeight());
        return createRenderer(bounds, "background.jpg");
    }

    private Renderable createTorch(DefaultParticleSystem particleSystem, DefaultDynamicLightMap lightMap) throws IOException {
        Rectangle bounds = Rectangle.create(32, 64).move(11, -22);
        ContentRenderer renderer = createRenderer(bounds, "torch.png");
        return new Torch(game, renderer, lightMap, particleSystem);
    }

    private LightRenderer createLightRenderer(Window window, LightMap lightMap) {
        DefaultLightRenderer lightRenderer = DefaultLightRenderer.create(window, this);
        lightRenderer.addLightMap(lightMap);
        lightRenderer.prepare(32, 32);
        return lightRenderer;
    }

    private DefaultParticleSystem createParticleSystem() throws IOException {
        ParticleSystemParameters params = new ResourceReader().read(getUrl("fire.particle"), ParticleSystemParameters::new);
        return DefaultParticleSystem.create(params, this);
    }

    @Override
    protected void initialize(FrameInitializer initializer) throws IOException {
        Window window = game.getWindow();
        DefaultParticleSystem particleSystem = createParticleSystem();
        DefaultDynamicLightMap lightMap = new DefaultDynamicLightMap(new DefaultLightSpreadMatrix(20));
        DefaultLayerObject<Renderable> frameLayer = new DefaultLayerObject<>();
        frameLayer.getBackground().add(createBackground(window));
        frameLayer.add(createTorch(particleSystem, lightMap));
        frameLayer.getForeground().add(particleSystem);
        LightRenderer lightRenderer = createLightRenderer(window, lightMap);
        frameLayer.setOverlay(alpha -> {
            lightRenderer.apply();
            lightRenderer.render(alpha);
            lightRenderer.reset();
        });
        add(frameLayer);
    }

    @Override
    protected void onInitialized(FrameInitializationContext context, Throwable error) {

    }

    @Override
    protected void onBegin() {

    }

    @Override
    protected void onEnd() {

    }

    @Override
    protected void onDispose() {

    }
}
