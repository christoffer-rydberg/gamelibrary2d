package com.gamelibrary2d.demos.drawing;

import com.gamelibrary2d.Game;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.random.RandomGenerator;
import com.gamelibrary2d.common.random.RandomInstance;
import com.gamelibrary2d.components.containers.DefaultLayer;
import com.gamelibrary2d.components.containers.Layer;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.components.frames.FrameInitializationContext;
import com.gamelibrary2d.components.frames.FrameInitializer;
import com.gamelibrary2d.framework.Mouse;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.framework.Window;
import com.gamelibrary2d.opengl.buffers.PositionBuffer;
import com.gamelibrary2d.opengl.renderers.*;
import com.gamelibrary2d.opengl.resources.MutableQuad;
import com.gamelibrary2d.opengl.resources.Quad;

public class DemoFrame extends AbstractFrame {
    private final Game game;

    public DemoFrame(Game game) {
        super(game);
        this.game = game;
    }

    @Override
    protected void onBegin(FrameInitializer initializer) {
        Window window = game.getWindow();

        Rectangle windowBounds = new Rectangle(0, 0, window.getWidth(), window.getHeight());

        RenderCache<Layer<Renderable>> objLayer = RenderCache.create(new DefaultLayer<>(), windowBounds, this);

        GeometryTool lineTool = new GeometryTool(
                Mouse.instance().mouseButton1(),
                () -> new Geometry(PositionBuffer.create(this), new LineRenderer(4f)),
                4f);

        lineTool.addGeometryCreatedListener(line -> {
            randomizeColor(line.getArrayRenderer());
            objLayer.getRenderer().add(line);
            objLayer.flushCache();
        });

        QuadTool quadTool = new QuadTool(
                Mouse.instance().mouseButton2(),
                MutableQuad.create(this),
                r -> Quad.create(r, this));

        quadTool.addQuadCreatedListener(quad -> {
            ContentRenderer renderer = new SurfaceRenderer<>(quad);
            randomizeColor(renderer);
            objLayer.getRenderer().add(renderer);
            objLayer.flushCache();
        });

        add(objLayer);
        add(lineTool);
        add(quadTool);
    }

    @Override
    protected void onInitializationFailed(Throwable error) {

    }

    @Override
    protected void onInitializationSuccessful(FrameInitializationContext context) {

    }

    private void randomizeColor(Renderer renderer) {
        RandomGenerator random = RandomInstance.get();
        renderer.setColor(
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat()
        );
    }

    @Override
    protected void onEnd() {

    }

    @Override
    protected void onDispose() {

    }
}