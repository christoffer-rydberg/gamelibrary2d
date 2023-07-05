package com.gamelibrary2d.demos.draganddrop;

import com.gamelibrary2d.*;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.opengl.resources.Quad;
import com.gamelibrary2d.opengl.resources.QuadShape;
import com.gamelibrary2d.opengl.resources.Surface;

public class DemoFrame extends AbstractFrame {
    private final Game game;
    private final DragAndDropLayer<DemoGameObject> dragAndDropLayer;

    public DemoFrame(Game game) {
        super(game);
        this.game = game;
        dragAndDropLayer = new DragAndDropLayer<>();
        dragAndDropLayer.addDragStartedPublisher((pointerId, obj) -> obj.onDragStarted());
        dragAndDropLayer.addDragFinishedPublisher((pointerId, obj) -> obj.onDragFinished());
    }

    @Override
    protected void onBegin() {
        DemoGameObject obj1 = createDemoObject(Rectangle.create(200, 200));
        obj1.setPosition(game.getWindow().getWidth()/2f, game.getWindow().getHeight()/2f);
        dragAndDropLayer.add(obj1);

        DemoGameObject obj2 = createDemoObject(Rectangle.create(100, 100));
        obj2.setPosition(game.getWindow().getWidth()/2f, game.getWindow().getHeight()/2f);
        dragAndDropLayer.add(obj2);

        add(dragAndDropLayer);
    }

    private DemoGameObject createDemoObject(Rectangle bounds) {
        Surface surface = Quad.create(bounds, QuadShape.RADIAL_GRADIENT, this);
        return new DemoGameObject(surface);
    }

    @Override
    protected void onEnd() {

    }

    @Override
    protected void onDispose() {

    }

}