package com.gamelibrary2d.demos.animation;

import com.gamelibrary2d.*;
import com.gamelibrary2d.animations.*;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.disposal.DefaultDisposer;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.lwjgl.imaging.FontMetadataFactory;
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

    @Override
    protected void onBegin() {
        final float windowWidth = game.getWindow().getWidth();
        final float windowHeight = game.getWindow().getHeight();

        GameObject loadButton = new LoadButton();
        loadButton.setPosition(windowWidth / 2, windowHeight - windowHeight / 6);

        animatedObject = new AnimatedGameObject(new AnimationRenderer(this));
        animatedObject.setPosition(windowWidth / 2, windowHeight / 2);

        add(animatedObject);
        add(loadButton);
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

    private class LoadButton extends AbstractGameObject implements PointerDownAware, PointerUpAware {
        private final Label label;
        private final Rectangle bounds;
        private final Point pointerPosition = new Point();
        private int pointerId = -1;
        private int pointerButton = -1;

        public LoadButton() {
            Font font = DefaultFont.create(
                    FontMetadataFactory.create(new java.awt.Font("Gabriola", java.awt.Font.BOLD, 48)),
                    DemoFrame.this);

            Label label = new Label(font, "Click here to load an animation");
            label.setAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.CENTER);
            label.setColor(Color.WHITE);

            this.label = label;
            this.bounds = label.calculateBounds();
        }

        @Override
        public Rectangle getBounds() {
            return bounds;
        }

        @Override
        protected void onRender(float alpha) {
            this.label.render(alpha);
        }

        @Override
        public boolean pointerDown(InputState inputState, int id, int button, float x, float y) {
            pointerPosition.set(x, y, this);
            if (getBounds().contains(pointerPosition)) {
                pointerId = id;
                pointerButton = button;
                return true;
            }

            return false;
        }

        @Override
        public void pointerUp(InputState inputState, int id, int button, float x, float y) {
            if (id == pointerId && button == pointerButton) {
                pointerId = -1;
                pointerButton = -1;

                if (loadingAnimation == null) {
                    try {
                        loadingAnimation = selectAnimation();
                    } catch (IOException e) {
                        onException(e);
                    }
                }
            }
        }
    }
}
