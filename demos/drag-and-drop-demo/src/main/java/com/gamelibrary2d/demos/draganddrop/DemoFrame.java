package com.gamelibrary2d.demos.draganddrop;

import com.gamelibrary2d.*;
import com.gamelibrary2d.components.frames.AbstractFrame;

public class DemoFrame extends AbstractFrame {
    private final Game game;
    private final DragAndDropLayer<DemoGameObject> dragAndDropLayer;

    public DemoFrame(Game game) {
        super(game);
        this.game = game;
        dragAndDropLayer = new DragAndDropLayer<>();
    }

    @Override
    protected void onBegin() {
        DemoGameObject obj1 = new DemoGameObject(Rectangle.create(200, 200), this);
        obj1.setPosition(game.getWindow().getWidth()/2f, game.getWindow().getHeight()/2f);
        dragAndDropLayer.add(obj1);

        DemoGameObject obj2 = new DemoGameObject(Rectangle.create(100, 100), this);
        obj2.setPosition(game.getWindow().getWidth()/2f, game.getWindow().getHeight()/2f);
        dragAndDropLayer.add(obj2);

        add(dragAndDropLayer);
    }

    @Override
    protected void onEnd() {

    }

    @Override
    protected void onDispose() {

    }

}