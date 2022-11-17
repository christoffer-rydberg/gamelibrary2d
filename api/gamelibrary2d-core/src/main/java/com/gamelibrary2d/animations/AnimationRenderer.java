package com.gamelibrary2d.animations;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.disposal.DefaultDisposer;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.common.denotations.Bounded;
import com.gamelibrary2d.opengl.ModelMatrix;
import com.gamelibrary2d.opengl.OpenGLState;
import com.gamelibrary2d.opengl.renderers.AbstractContentRenderer;
import com.gamelibrary2d.opengl.renderers.BlendMode;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.*;
import com.gamelibrary2d.opengl.shaders.ShaderParameter;
import com.gamelibrary2d.opengl.shaders.ShaderProgram;

public class AnimationRenderer extends AbstractContentRenderer implements Bounded {
    private final Disposer disposer;
    private Animation animation;
    private AnimationBackgroundBuffer backgroundBuffer;
    private boolean looping;
    private int previousFrameIndex = -1;
    private float globalFrameDuration = -1;

    public AnimationRenderer(Disposer disposer) {
        this.disposer = disposer;
    }

    public AnimationRenderer(Animation animation, boolean loop, Disposer disposer) {
        this.disposer = disposer;
        setAnimation(animation, loop);
    }

    private static boolean requiresBackgroundBuffering(Animation animation) {
        for (AnimationFrame frame : animation.getFrames()) {
            if (frame.getRenderToBackgroundHint())
                return true;
        }
        return false;
    }

    private static void render(ShaderProgram shaderProgram, AnimationFrame frame) {
        frame.getTexture().bind();
        frame.getSurface().render(shaderProgram);
    }

    private float getDuration(Animation animation) {
        return globalFrameDuration > 0f
                ? globalFrameDuration * animation.getFrames().size()
                : animation.getDuration();
    }

    private float getAnimationTime(Animation animation, boolean looping) {
        float duration = getDuration(animation);
        float timeParameter = getShaderParameter(ShaderParameter.TIME);
        if (looping) {
            int roundTrips = (int) (timeParameter / duration);
            return timeParameter - roundTrips * duration;
        } else {
            return Math.min(timeParameter, duration);
        }
    }

    public Animation getAnimation() {
        return animation;
    }

    public void setAnimation(Animation animation, boolean loop) {
        this.looping = loop;
        if (this.animation != animation) {
            this.animation = animation;
            this.previousFrameIndex = -1;
        }
    }

    /**
     * The global frame duration determines the duration of each frame.
     * It can be enabled by invoking {@link #setGlobalFrameDuration} and disabled by invoking {@link #disableGlobalFrameDuration}.
     * If disabled, the {@link AnimationFrame#getDurationHint() duration hint} for each frame will be respected.
     *
     * @return The global frame duration or -1 if disabled.
     */
    public float getGlobalFrameDuration() {
        return this.globalFrameDuration;
    }

    /**
     * Sets the {@link #getGlobalFrameDuration() global frame duration}.
     */
    public void setGlobalFrameDuration(float duration) {
        if (duration <= 0f) {
            throw new IllegalStateException("The global frame duration must be greater than 0");
        }

        setGlobalFrameDurationInternal(duration);
    }

    /**
     * Disables the {@link #getGlobalFrameDuration() global frame duration}.
     */
    public void disableGlobalFrameDuration() {
        setGlobalFrameDurationInternal(-1);
    }

    private void setGlobalFrameDurationInternal(float globalFrameDuration) {
        if (this.globalFrameDuration != globalFrameDuration) {
            this.globalFrameDuration = globalFrameDuration;
            this.previousFrameIndex = -1;
        }
    }

    public boolean isLooping() {
        return looping;
    }

    private int getFrameIndex(Animation animation, boolean looping, int previousIndex) {
        float duration = getDuration(animation);
        if (duration <= 0f) {
            return Math.max(previousIndex, 0);
        }

        float time = getAnimationTime(animation, looping);
        if (!looping && time == getDuration(animation)) {
            return animation.getFrames().size() - 1;
        } else if (globalFrameDuration > 0f) {
            return (int) (time / globalFrameDuration);
        } else {
            if (previousIndex > 0) {
                // Try previous frame again:
                if (animation.isFrameActive(previousIndex, time)) {
                    return previousIndex;
                }

                int nextFrame = (previousIndex + 1) % animation.getFrames().size();

                // Try next frame:
                if (animation.isFrameActive(nextFrame, time)) {
                    return nextFrame;
                }
            }

            // Search for frame:
            return animation.getFrameIndex(time);
        }
    }

    private int getFrameIndex(Animation animation, boolean looping) {
        previousFrameIndex = getFrameIndex(animation, looping, previousFrameIndex);
        return previousFrameIndex;
    }

    public AnimationFrame getFrame() {
        Animation animation = getAnimation();
        return animation != null ? animation.getFrame(getFrameIndex(animation, looping)) : null;
    }

    public boolean isAnimationFinished() {
        Animation animation = getAnimation();
        if (animation == null) {
            return true;
        }

        return !looping && getFrameIndex(animation, false) == animation.getFrames().size() - 1;
    }

    @Override
    public Rectangle getBounds() {
        Animation animation = getAnimation();
        return animation != null ? animation.getBounds() : Rectangle.EMPTY;
    }

    @Override
    protected ShaderProgram prepareShaderProgram(float alpha) {
        setShaderParameter(ShaderParameter.TEXTURED, 1);
        return super.prepareShaderProgram(alpha);
    }

    private void prepareBackgroundBuffer() {
        if (backgroundBuffer != null) {
            if (backgroundBuffer.animation == animation) {
                return;
            }

            backgroundBuffer.dispose();
            backgroundBuffer = null;
        }

        if (animation != null && requiresBackgroundBuffering(animation)) {
            backgroundBuffer = new AnimationBackgroundBuffer(animation, disposer);
        }
    }

    @Override
    protected void onRender(ShaderProgram shaderProgram) {
        Animation animation = getAnimation();
        if (animation != null) {
            prepareBackgroundBuffer();

            int frameIndex = getFrameIndex(animation, looping);

            AnimationFrame activeFrame = animation.getFrame(frameIndex);
            if (backgroundBuffer != null) {
                backgroundBuffer.render(shaderProgram, frameIndex);

                if (!activeFrame.getRenderToBackgroundHint()) {
                    render(shaderProgram, activeFrame);
                }
            } else {
                render(shaderProgram, activeFrame);
            }
        }
    }

    private static class AnimationBackgroundBuffer {
        private final Animation animation;
        private final DefaultDisposer resourceDisposer;
        private FrameBuffer frameBuffer;
        private SurfaceRenderer<?> frameBufferRenderer;
        private FrameRenderer[] frameRenderers;
        private int previousFrame;

        AnimationBackgroundBuffer(Animation animation, Disposer disposer) {
            this.animation = animation;
            resourceDisposer = new DefaultDisposer(disposer);
        }

        private FrameRenderer[] createFrameRenderers() {
            int size = animation.getFrames().size();
            FrameRenderer[] frameRenderers = new FrameRenderer[size];
            for (int i = 0; i < size; ++i) {
                AnimationFrame frame = animation.getFrame(i);
                Surface frameSurface = frame.getSurface();
                Texture frameTexture = frame.getTexture();

                Rectangle animationBounds = animation.getBounds();
                Rectangle frameBounds = frameSurface.getBounds();
                float textureWidth = frameTexture.getWidth();
                float textureHeight = frameTexture.getHeight();

                float invertedScaleX = textureWidth / frameBounds.getWidth();
                float invertedScaleY = textureHeight / frameBounds.getHeight();

                frameRenderers[i] = new FrameRenderer(
                        frameSurface,
                        -animationBounds.getLowerX(),
                        -animationBounds.getLowerY(),
                        invertedScaleX,
                        invertedScaleY
                );
            }

            return frameRenderers;
        }

        private Texture createBackgroundTexture(Animation animation) {
            float xMin = Float.MAX_VALUE, yMin = Float.MAX_VALUE;
            float xMax = Float.MIN_VALUE, yMax = Float.MIN_VALUE;
            for (AnimationFrame frame : animation.getFrames()) {
                Rectangle bounds = frame.getBounds();

                Texture texture = frame.getTexture();
                float width = texture.getWidth();
                float height = texture.getHeight();

                Rectangle imageBounds = bounds.resize(width / bounds.getWidth(), height / bounds.getHeight());
                xMin = Math.min(xMin, imageBounds.getLowerX());
                yMin = Math.min(yMin, imageBounds.getLowerY());
                xMax = Math.max(xMax, imageBounds.getUpperX());
                yMax = Math.max(yMax, imageBounds.getUpperY());
            }

            return DefaultTexture.create(
                    Math.round(xMax - xMin),
                    Math.round(yMax - yMin),
                    resourceDisposer);
        }

        private void render(ShaderProgram shaderProgram, int currentFrame) {
            if (frameBuffer == null) {
                // Create OpenGL resources when rendering for the first time
                Texture texture = createBackgroundTexture(animation);
                frameBuffer = DefaultFrameBuffer.create(texture, resourceDisposer);
                frameBufferRenderer = new SurfaceRenderer<>(
                        Quad.create(animation.getBounds(), resourceDisposer),
                        texture);
                frameRenderers = createFrameRenderers();
            }

            // Prepare model matrix
            ModelMatrix modelMatrix = ModelMatrix.instance();
            modelMatrix.pushMatrix();
            modelMatrix.clearMatrix();

            float alpha = shaderProgram.getParameter(ShaderParameter.ALPHA);
            BlendMode blendMode = OpenGLState.getBlendMode();

            // Disable blend mode
            OpenGLState.setBlendMode(BlendMode.TRANSPARENT);

            // Render to background buffer
            renderToFrameBuffer(shaderProgram, currentFrame);

            // Restore model matrix
            modelMatrix.popMatrix();

            // Render frame buffer texture to the default frame buffer
            frameBufferRenderer.setShaderProgram(shaderProgram);
            frameBufferRenderer.setBlendMode(blendMode);
            frameBufferRenderer.render(alpha);
        }

        private void renderToFrameBuffer(ShaderProgram shaderProgram, int currentFrame) {
            int previousFbo = frameBuffer.bind();
            try {
                if (currentFrame < previousFrame) {
                    frameBuffer.clear();
                    previousFrame = 0;
                }

                for (int i = previousFrame; i <= currentFrame; ++i) {
                    AnimationFrame frame = animation.getFrame(i);

                    if (frame.getRestoreBackgroundHint()) {
                        frameBuffer.clear();
                    }

                    if (frame.getRenderToBackgroundHint()) {
                        if (shaderProgram.setParameter(ShaderParameter.ALPHA, 1f)) {
                            shaderProgram.applyParameters();
                        }

                        frame.getTexture().bind();
                        frameRenderers[i].render(shaderProgram);
                    }
                }
                previousFrame = currentFrame;
            } finally {
                OpenGLState.bindFrameBuffer(previousFbo);
            }
        }

        void dispose() {
            if (frameBuffer != null) {
                resourceDisposer.dispose();
                frameBuffer = null;
                frameBufferRenderer = null;
                frameRenderers = null;
            }
        }

        private static class FrameRenderer {
            private final Surface frameSurface;
            private final float translationX;
            private final float translationY;
            private final float scaleX;
            private final float scaleY;

            FrameRenderer(Surface frameSurface, float translationX, float translationY, float scaleX, float scaleY) {
                this.frameSurface = frameSurface;
                this.translationX = translationX;
                this.translationY = translationY;
                this.scaleX = scaleX;
                this.scaleY = scaleY;
            }

            void render(ShaderProgram shaderProgram) {
                ModelMatrix modelMatrix = ModelMatrix.instance();
                modelMatrix.pushMatrix();
                modelMatrix.scalef(scaleX, scaleY, 0f);
                modelMatrix.translatef(translationX, translationY, 0f);
                shaderProgram.updateModelMatrix();
                frameSurface.render(shaderProgram);
                modelMatrix.popMatrix();
            }
        }
    }
}
