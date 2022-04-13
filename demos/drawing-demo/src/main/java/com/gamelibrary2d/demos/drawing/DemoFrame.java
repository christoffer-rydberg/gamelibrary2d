package com.gamelibrary2d.demos.drawing;

import com.gamelibrary2d.Game;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.random.RandomGenerator;
import com.gamelibrary2d.common.random.RandomInstance;
import com.gamelibrary2d.components.containers.BasicLayer;
import com.gamelibrary2d.components.containers.Layer;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.components.frames.FrameInitializationContext;
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
        this.game = game;
    }

    @Override
    protected void onInitialize(FrameInitializationContext context) {
        Window window = game.getWindow();

        Rectangle windowBounds = new Rectangle(0, 0, window.getWidth(), window.getHeight());

        RenderCache<Layer<Renderable>> objLayer = RenderCache.create(new BasicLayer<>(), windowBounds, this);

        GeometryTool lineTool = new GeometryTool(
                Mouse.instance().mouseButton1(),
                () -> new Geometry(PositionBuffer.create(this), new LineRenderer(4f)),
                4f);

        lineTool.addGeometryCreatedListener(line -> {
            randomizeColor(line.getRenderer());
            objLayer.getContent().add(line);
            objLayer.flushCache();
        });

        QuadTool quadTool = new QuadTool(
                Mouse.instance().mouseButton2(),
                MutableQuad.create(this),
                r -> Quad.create(r, this));

        quadTool.addQuadCreatedListener(quad -> {
            ContentRenderer renderer = new SurfaceRenderer<>(quad);
            randomizeColor(renderer);
            objLayer.getContent().add(renderer);
            objLayer.flushCache();
        });

        add(objLayer);
        add(lineTool);
        add(quadTool);
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
    protected void onLoad(FrameInitializationContext context) {

    }

    @Override
    protected void onLoaded(FrameInitializationContext context) {

    }

    @Override
    protected void onBegin() {

    }

    @Override
    protected void onEnd() {

    }
}