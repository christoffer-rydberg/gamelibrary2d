package com.gamelibrary2d.demos.transparentwindow;

import com.gamelibrary2d.Game;
import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.components.frames.InitializationContext;
import com.gamelibrary2d.components.objects.AnimatedGameObject;
import com.gamelibrary2d.framework.Window;
import com.gamelibrary2d.imaging.AnimationFrameMetadata;
import com.gamelibrary2d.imaging.AnimationLoader;
import com.gamelibrary2d.imaging.AnimationMetadata;
import com.gamelibrary2d.imaging.StandardAnimationFormats;
import com.gamelibrary2d.renderers.AnimationRenderer;
import com.gamelibrary2d.resources.Animation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DemoFrame extends AbstractFrame {

    private final Game game;
    private AnimatedGameObject<AnimationRenderer> animationObj;

    DemoFrame(Game game) {
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
    protected void onInitialize(InitializationContext context) {
        Window window = game.getWindow();
        try {
            Animation animation = createAnimation();
            animationObj = new AnimatedGameObject<>(new AnimationRenderer(animation, false, this));
            animationObj.setPosition(window.getWidth() / 2f, window.getHeight() / 2f);
            add(animationObj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onLoad(InitializationContext context) {

    }

    @Override
    protected void onLoaded(InitializationContext context) {

    }

    @Override
    protected void onBegin() {

    }

    @Override
    protected void onEnd() {

    }

    @Override
    protected void onUpdate(float deltaTime) {
        super.onUpdate(deltaTime);
        if (animationObj.getContent().isAnimationFinished()) {
            game.exit();
        }
    }
}
