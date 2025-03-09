package com.gamelibrary2d.demos.drawing;

import com.gamelibrary2d.*;
import com.gamelibrary2d.components.containers.DefaultLayer;
import com.gamelibrary2d.components.containers.Layer;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.input.Mouse;
import com.gamelibrary2d.opengl.buffers.PositionBuffer;
import com.gamelibrary2d.opengl.renderers.*;
import com.gamelibrary2d.opengl.resources.MutableQuad;
import com.gamelibrary2d.opengl.resources.Quad;
import com.gamelibrary2d.random.RandomGenerator;
import com.gamelibrary2d.random.RandomInstance;

public class DemoFrame extends AbstractFrame {
    private final Game game;

    public DemoFrame(Game game) {
        super(game);
        this.game = game;
    }

    @Override
    protected void onBegin() {
        Window window = game.getWindow();

        Rectangle windowBounds = new Rectangle(0, 0, window.getWidth(), window.getHeight());

        Canvas<Layer<Renderable>> canvas = Canvas.create(new DefaultLayer<>(), windowBounds, false,this);

        GeometryTool lineTool = new GeometryTool(
                Mouse.instance().mouseButton1(),
                () -> new Geometry(PositionBuffer.create(this), new LineRenderer(4f)),
                4f);

        lineTool.addGeometryCreatedListener(line -> {
            randomizeColor(line.getArrayRenderer());
            canvas.getContent().add(line);
            canvas.refresh();
        });

        QuadTool quadTool = new QuadTool(
                Mouse.instance().mouseButton2(),
                MutableQuad.create(this),
                r -> Quad.create(r, this));

        quadTool.addQuadCreatedListener(quad -> {
            ContentRenderer renderer = new SurfaceRenderer<>(quad);
            randomizeColor(renderer);
            canvas.getContent().add(renderer);
            canvas.refresh();
        });

        add(canvas);
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
    protected void onEnd() {

    }

    @Override
    protected void onDispose() {

    }
}