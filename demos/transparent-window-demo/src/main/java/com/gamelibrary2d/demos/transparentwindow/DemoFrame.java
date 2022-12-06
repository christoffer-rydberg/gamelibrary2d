package com.gamelibrary2d.demos.transparentwindow;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.Game;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.Window;
import com.gamelibrary2d.animations.*;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.components.frames.FrameInitializationContext;
import com.gamelibrary2d.components.frames.FrameInitializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DemoFrame extends AbstractFrame {
    private final Game game;
    private AnimatedGameObject animationObj;

    DemoFrame(Game game) {
        super(game);
        this.game = game;
        setBackgroundColor(Color.TRANSPARENT);
    }

    private Animation createAnimation() throws IOException {
        AnimationMetadata animationMetadata = AnimationLoader.load(
                DemoFrame.class.getResource("/Images/homer.gif"),
                StandardAnimationFormats.GIF);

        Rectangle bounds = animationMetadata.calculateBounds()
                .resize(Rectangle.create(1f, 1f))
                .restrict(game.getWindow().getWidth(), game.getWindow().getHeight());

        // Modify frames to reverse back when finished:
        List<AnimationFrameMetadata> originalFrames = new ArrayList<>(animationMetadata.getFrames());
        Collections.reverse(animationMetadata.getFrames());
        animationMetadata.getFrames().addAll(originalFrames);

        return Animation.create(animationMetadata, bounds, this);
    }

    @Override
    protected void onInitialize(FrameInitializer initializer) {
        Window window = game.getWindow();
        try {
            Animation animation = createAnimation();
            animationObj = new AnimatedGameObject(new AnimationRenderer(animation, false, this));
            animationObj.setPosition(window.getWidth() / 2f, window.getHeight() / 2f);
            add(animationObj);
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

    @Override
    protected void onUpdate(float deltaTime) {
        super.onUpdate(deltaTime);
        if (animationObj.getRenderer().isAnimationFinished()) {
            game.exit();
        }
    }
}
