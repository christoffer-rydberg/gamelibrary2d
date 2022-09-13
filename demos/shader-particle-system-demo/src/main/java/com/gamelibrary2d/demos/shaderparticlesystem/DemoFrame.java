package com.gamelibrary2d.demos.shaderparticlesystem;

import com.gamelibrary2d.Game;
import com.gamelibrary2d.components.containers.DefaultLayerGameObject;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.components.frames.FrameInitializationContext;
import com.gamelibrary2d.components.frames.FrameInitializer;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.framework.Window;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class DemoFrame extends AbstractFrame {

    private final Game game;

    DemoFrame(Game game) {
        super(game);
        this.game = game;
    }

    @Override
    protected void onBegin(FrameInitializer initializer) {
        try {
            Window window = game.getWindow();
            float windowWidth = window.getWidth();
            float windowHeight = window.getHeight();
            BufferedImage init = ImageIO.read(DemoFrame.class.getResource("/Images/before.jpg"));
            BufferedImage goal = ImageIO.read(DemoFrame.class.getResource("/Images/after.jpg"));

            Renderable particleSystem = ParticleSystemFactory.create(init, goal, windowWidth / 2f,
                    windowHeight / 2f, this);

            // Adjust scale to fit on screen
            float scaleX = Math.min(1f, 0.5f * windowWidth / init.getWidth());
            float scaleY = Math.min(1f, 0.5f * windowHeight / init.getHeight());
            float scale = Math.min(scaleX, scaleY);

            DefaultLayerGameObject<Renderable> layer = new DefaultLayerGameObject<>();
            layer.setScale(scale, scale);
            layer.setScaleAndRotationAnchor(windowWidth / 2, windowHeight / 2);
            layer.add(particleSystem);
            add(layer);
        } catch (IOException e) {
            e.printStackTrace();
        }
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