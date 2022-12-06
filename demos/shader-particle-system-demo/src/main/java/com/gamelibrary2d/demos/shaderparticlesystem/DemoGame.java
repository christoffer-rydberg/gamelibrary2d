package com.gamelibrary2d.demos.shaderparticlesystem;

import com.gamelibrary2d.AbstractGame;
import com.gamelibrary2d.lwjgl.Lwjgl_Framework;

public class DemoGame extends AbstractGame {

    DemoGame() {
        super(new Lwjgl_Framework());
    }

    @Override
    protected void onStart() {
        setFrame(new DemoFrame(this));
    }

    @Override
    protected void onExit() {
        // TODO Auto-generated method stub
    }
}