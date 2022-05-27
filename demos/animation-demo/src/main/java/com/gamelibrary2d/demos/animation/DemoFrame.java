package com.gamelibrary2d.demos.animation;

import com.gamelibrary2d.Game;
import com.gamelibrary2d.animations.*;
import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.disposal.DefaultDisposer;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.components.DefaultObservableGameObject;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.components.frames.FrameInitializationContext;
import com.gamelibrary2d.components.frames.FrameInitializer;
import com.gamelibrary2d.framework.lwjgl.FontMetadataFactory;
import com.gamelibrary2d.text.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

public class DemoFrame extends AbstractFrame {
    private static final float DEFAULT_FRAME_DURATION = 0.05f;

    private final Game game;
    private final Disposer animationDisposer = new DefaultDisposer(this);
    private Future<AnimationMetadata> loadingAnimation;
    private AnimatedGameObject animatedObject;

    DemoFrame(Game game) {
        super(game);
        this.game = game;
    }

    private GameObject createLoadButton() {
        Font font = DefaultFont.create(
                FontMetadataFactory.create(new java.awt.Font("Gabriola", java.awt.Font.BOLD, 48)),
                this);

        Label label = new Label(font, "Click here to load an animation");
        label.setAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.CENTER);
        label.setColor(Color.WHITE);

        DefaultObservableGameObject<Label> loadButton = new DefaultObservableGameObject<>();
        loadButton.setContent(label);
        loadButton.setBounds(label.calculateBounds());
        loadButton.addPointerUpListener(this::onLoadButtonClicked);

        return loadButton;
    }

    @Override
    protected void onBegin(FrameInitializer initializer) {
        final float windowWidth = game.getWindow().getWidth();
        final float windowHeight = game.getWindow().getHeight();

        GameObject loadButton = createLoadButton();
        loadButton.setPosition(windowWidth / 2, windowHeight - windowHeight / 6);

        animatedObject = new AnimatedGameObject(new AnimationRenderer(this));
        animatedObject.setPosition(windowWidth / 2, windowHeight / 2);

        add(animatedObject);
        add(loadButton);
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

    private AnimationMetadata loadAnimation(File file) throws IOException {
        if (file.isDirectory()) {
            return AnimationLoader.load(
                    file.toPath(),
                    Pattern.compile("^*.(png|jpe?g)$"),
                    DEFAULT_FRAME_DURATION);
        } else {
            return AnimationLoader.load(file.toURI().toURL(), StandardAnimationFormats.GIF);
        }
    }

    private Future<AnimationMetadata> loadAnimationAsync(File file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadAnimation(file);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });
    }

    private Future<AnimationMetadata> selectAnimation() throws IOException {
        FileChooser fileChooser = new FileChooser(System.getenv("TEMP") + "/animation_demo/file_chooser_path.txt");
        File file = fileChooser.browse(FileSelectionMode.FILES_AND_DIRECTORIES);
        return file != null ? loadAnimationAsync(file) : null;
    }

    private void onException(Exception e) {
        e.printStackTrace();
        game.exit();
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (loadingAnimation != null) {
            if (loadingAnimation.isDone()) {
                try {
                    animationDisposer.dispose();

                    AnimationMetadata animationMetadata = loadingAnimation.get();

                    Animation animation = Animation.create(
                            animationMetadata,
                            animationMetadata.calculateBounds()
                                    .resize(Rectangle.create(1f, 1f))
                                    .restrict(game.getWindow().getWidth(), game.getWindow().getHeight()),
                            animationDisposer);

                    animatedObject.getRenderer().setAnimation(animation, true);

                    boolean hasInvalidFrameDurations = animation.getFrames()
                            .stream()
                            .anyMatch(frame -> frame.getDurationHint() <= 0);

                    if (hasInvalidFrameDurations) {
                        animatedObject.getRenderer().setGlobalFrameDuration(0.1f);
                    } else {
                        animatedObject.getRenderer().disableGlobalFrameDuration();
                    }

                    animatedObject.setEnabled(false);
                    invokeLater(() -> invokeLater(() -> {
                        animatedObject.setEnabled(true);
                        animatedObject.setAnimationTime(0f);
                    }));

                    loadingAnimation = null;
                } catch (InterruptedException | ExecutionException e) {
                    onException(e);
                }
            } else if (loadingAnimation.isCancelled()) {
                loadingAnimation = null;
            }
        }

        super.onUpdate(deltaTime);
    }

    private void onLoadButtonClicked(int id, int button, float x, float y, float projectedX, float projectedY) {
        if (loadingAnimation == null) {
            try {
                loadingAnimation = selectAnimation();
            } catch (IOException e) {
                onException(e);
            }
        }
    }
}
