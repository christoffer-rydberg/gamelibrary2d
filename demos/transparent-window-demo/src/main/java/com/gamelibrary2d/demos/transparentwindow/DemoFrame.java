package com.gamelibrary2d.demos.transparentwindow;

import com.gamelibrary2d.Game;
import com.gamelibrary2d.animation.*;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.frames.AbstractFrame;
import com.gamelibrary2d.objects.ComposableObject;
import com.gamelibrary2d.renderers.AnimationRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class DemoFrame extends AbstractFrame {

    private ComposableObject<AnimationRenderer> animationObj;

    DemoFrame(Game game) {
        super(game);
    }

    private Animation createAnimation() throws IOException {
        var animation = AnimationFactory.create(DemoFrame.class.getResource("/Images/homer.gif"),
                AnimationFormats.GIF, Rectangle.centered(1f, 1f), AnimationFactory.NO_CONSTRAINTS, this);
        var frames = animation.getFrames();
        var frameUpdate = new ArrayList<AnimationFrame>();
        frames.forEach(frameUpdate::add);
        Collections.reverse(frameUpdate);
        frames.forEach(frameUpdate::add);
        return new Animation(frameUpdate, animation.getBounds());
    }

    @Override
    protected void onInitialize(FrameInitializer initializer) {
        var game = getGame();
        var window = game.getWindow();
        try {
            var animation = createAnimation();
            animationObj = new AnimatedObject<>(new AnimationRenderer(animation, false, this));
            animationObj.getPosition().set(window.width() / 2f, window.height() / 2f);
            add(animationObj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        super.onUpdate(deltaTime);
        if (animationObj.getContent().isAnimationFinished()) {
            getGame().exit();
        }
    }

}