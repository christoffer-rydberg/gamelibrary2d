package com.gamelibrary2d.demos.draganddrop;

import com.gamelibrary2d.*;
import com.gamelibrary2d.components.frames.AbstractFrame;

public class DemoFrame extends AbstractFrame {
    private final Game game;

    public DemoFrame(Game game) {
        super(game);
        this.game = game;
    }

    @Override
    protected void onBegin() {
        DemoGameObject obj1 = new DemoGameObject(Rectangle.create(200, 200), this);
        obj1.setPosition(game.getWindow().getWidth()/2f, game.getWindow().getHeight()/2f);
        add(obj1);

        DemoGameObject obj2 = new DemoGameObject(Rectangle.create(100, 100), this);
        obj2.setPosition(game.getWindow().getWidth()/2f, game.getWindow().getHeight()/2f);
        add(obj2);
    }

    @Override
    protected void onEnd() {

    }

    @Override
    protected void onDispose() {

    }

}