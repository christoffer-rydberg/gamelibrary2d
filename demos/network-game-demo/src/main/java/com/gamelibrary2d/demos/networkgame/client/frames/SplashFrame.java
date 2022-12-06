package com.gamelibrary2d.demos.networkgame.client.frames;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.Game;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.components.frames.FrameInitializationContext;
import com.gamelibrary2d.components.frames.FrameInitializer;
import com.gamelibrary2d.demos.networkgame.client.ResourceManager;

public class SplashFrame extends AbstractFrame {

    public SplashFrame(Game game, ResourceManager resourceManager) {
        super(game);
        setBackgroundColor(Color.WHITE);
    }


    @Override
    protected void onInitialize(FrameInitializer initializer) {

    }

    @Override
    protected void onInitializationFailed(Throwable error) {

    }

    @Override
    protected void onInitializationSuccessful(FrameInitializationContext context) {

    }

    @Override
    protected void onEnd() {

    }

    @Override
    protected void onDispose() {

    }
}
