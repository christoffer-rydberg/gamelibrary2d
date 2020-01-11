package com.gamelibrary2d.renderers;

import com.gamelibrary2d.animation.Animation;
import com.gamelibrary2d.animation.AnimationFrame;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.common.disposal.ResourceDisposer;
import com.gamelibrary2d.common.io.BufferUtils;
import com.gamelibrary2d.framework.OpenGL;
import com.gamelibrary2d.glUtil.FrameBuffer;
import com.gamelibrary2d.glUtil.ModelMatrix;
import com.gamelibrary2d.glUtil.ShaderProgram;
import com.gamelibrary2d.rendering.RenderSettings;
import com.gamelibrary2d.resources.Quad;
import com.gamelibrary2d.resources.Texture;

import java.nio.ByteBuffer;

public class AnimationRenderer extends AbstractShaderRenderer {
    private final Disposer disposer;
    private Animation animation;
    private boolean looping;
    private float frameDuration; // TODO: Respect individual frame duration
    private BackgroundBuffer backgroundBuffer;

    public AnimationRenderer(Animation animation, boolean loop) {
        disposer = null;
        setAnimation(animation, loop);
        updateSettings(RenderSettings.TEXTURED, 1);
    }

    public AnimationRenderer(Animation animation, boolean loop, Disposer disposer) {
        this.disposer = disposer;
        setAnimation(animation, loop);
        updateSettings(RenderSettings.TEXTURED, 1);
    }

    public Animation getAnimation() {
        return animation;
    }

    private boolean requiresBackgroundBuffering() {
        for (var frame : animation.getFrames()) {
            if (frame.getRenderToBackgroundHint())
                return true;
        }
        return false;
    }

    private float calcAverageFrameDuration() {
        float duration = 0;
        for (var frame : animation.getFrames()) {
            duration += frame.getDurationHint();
        }
        return duration / animation.getFrameCount();
    }

    private void setAnimation(Animation animation, boolean loop) {
        this.animation = animation;
        this.looping = loop;
        this.frameDuration = calcAverageFrameDuration();

        if (backgroundBuffer != null) {
            backgroundBuffer.dispose();
            backgroundBuffer = null;
        }

        if (requiresBackgroundBuffering()) {
            backgroundBuffer = new BackgroundBuffer(disposer);
        }
    }

    /**
     * The duration of each frame in seconds.
     */
    public float getFrameDuration() {
        return frameDuration;
    }

    /**
     * Sets the {@link #getFrameDuration frame duration}.
     *
     * @param duration The duration of each frame in seconds.
     */
    public void setFrameDuration(float duration) {
        this.frameDuration = duration;
    }

    public boolean isLooping() {
        return looping;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public int getCurrentFrameIndex() {
        var size = animation.getFrameCount();
        if (size == 0)
            return -1;
        var index = getIndex();
        return looping ? index % size : Math.min(index, size - 1);
    }

    public AnimationFrame getCurrentFrame() {
        int index = getCurrentFrameIndex();
        return index < 0 ? null : animation.getFrame(index);
    }

    public boolean isAnimationFinished() {
        return !looping && getIndex() >= animation.getFrameCount();
    }

    private int getIndex() {
        float[] settings = getSettings();
        float time = settings == null ? 0 : settings[RenderSettings.TIME];
        return (int) (time / frameDuration);
    }

    @Override
    public Rectangle getBounds() {
        return animation.getBounds();
    }

    @Override
    public boolean isVisible(float x, float y) {
        var currentFrame = getCurrentFrame();
        if (currentFrame != null && InternalHitDetection.isVisible(currentFrame.getQuad(), currentFrame.getTexture(), x, y)) {
            return true;
        }

        return backgroundBuffer != null && backgroundBuffer.isVisible(x, y);
    }

    @Override
    protected void onRender(ShaderProgram shaderProgram) {
        var activeIndex = getCurrentFrameIndex();
        if (activeIndex < 0)
            return;

        var activeFrame = animation.getFrame(activeIndex);
        if (backgroundBuffer != null) {
            backgroundBuffer.render(shaderProgram, animation, activeIndex);

            if (!activeFrame.getRenderToBackgroundHint()) {
                // TODO: Alpha will blend incorrectly. Use another frame buffer as intermediate buffer before rendering?
                render(shaderProgram, activeFrame);
            }
        } else {
            render(shaderProgram, activeFrame);
        }
    }

    private void render(ShaderProgram shaderProgram, AnimationFrame frame) {
        frame.getTexture().bind();
        frame.getQuad().render(shaderProgram);
    }

    private static class BackgroundBuffer {
        private final ResourceDisposer resourceDisposer;
        private FrameBuffer frameBuffer;
        private Renderer frameBufferRenderer;
        private Quad[] backgroundQuads;
        private int previousFrame;

        BackgroundBuffer(Disposer disposer) {
            resourceDisposer = new ResourceDisposer(disposer);
        }

        private static Quad[] createBackgroundQuads(Animation animation, Disposer disposer) {
            var quads = new Quad[animation.getFrameCount()];
            for (int i = 0; i < animation.getFrameCount(); ++i) {
                var frame = animation.getFrame(i);

                var quad = frame.getQuad();
                var bounds = quad.getBounds();
                var scaledWidth = bounds.getWidth();
                var scaledHeight = bounds.getHeight();

                var width = frame.getTexture().getImageWidth();
                var height = frame.getTexture().getImageHeight();
                var offsetX = Math.round((width / scaledWidth) * (bounds.getXMin() - animation.getBounds().getXMin()));
                var offsetY = Math.round((height / scaledHeight) * (bounds.getYMin() - animation.getBounds().getYMin()));

                var fullSizeBounds = new Rectangle(offsetX, offsetY, width + offsetX, height + offsetY);
                quads[i] = Quad.create(fullSizeBounds, disposer);
            }
            return quads;
        }

        private void render(ShaderProgram shaderProgram, Animation animation, int activeIndex) {
            // Allocate resources when rendered for the first time
            if (frameBuffer == null) {
                var texture = Texture.create(animation.getOriginalWidth(), animation.getOriginalHeight(), resourceDisposer);
                var quad = Quad.create(animation.getBounds(), resourceDisposer);
                frameBuffer = FrameBuffer.create(texture, resourceDisposer);
                frameBufferRenderer = new SurfaceRenderer(quad, texture);
                backgroundQuads = createBackgroundQuads(animation, resourceDisposer);
            }

            // Prepare model matrix
            var modelMatrix = ModelMatrix.instance();
            modelMatrix.pushMatrix();
            modelMatrix.clearMatrix();
            shaderProgram.updateModelMatrix(ModelMatrix.instance());

            // Render to background buffer
            renderToFrameBuffer(shaderProgram, animation, activeIndex);

            // Restore model matrix
            modelMatrix.popMatrix();
            shaderProgram.updateModelMatrix(ModelMatrix.instance());

            // Render frame buffer texture to the default frame buffer
            float alpha = shaderProgram.getSetting(RenderSettings.ALPHA);
            frameBufferRenderer.render(alpha);
        }

        private void renderToFrameBuffer(ShaderProgram shaderProgram, Animation animation, int activeIndex) {
            frameBuffer.bind();

            if (activeIndex < previousFrame) {
                frameBuffer.clear();
                previousFrame = 0;
            }

            float alpha = shaderProgram.getSetting(RenderSettings.ALPHA);
            for (int i = previousFrame; i <= activeIndex; ++i) {
                var frame = animation.getFrame(i);

                if (frame.getRestoreBackgroundHint()) {
                    frameBuffer.clear();
                }

                if (frame.getRenderToBackgroundHint()) {
                    // Make opaque. Alpha will be applied when rendered to default frame buffer.
                    if (shaderProgram.updateSetting(RenderSettings.ALPHA, 1f)) {
                        shaderProgram.applySettings();
                    }
                    frame.getTexture().bind();
                    backgroundQuads[i].render(shaderProgram);
                }
            }
            previousFrame = activeIndex;

            // Restore alpha setting
            if (shaderProgram.updateSetting(RenderSettings.ALPHA, alpha)) {
                shaderProgram.applySettings();
            }

            frameBuffer.unbind(true);
        }

        void dispose() {
            if (frameBuffer != null) {
                resourceDisposer.dispose();
                frameBuffer = null;
                frameBufferRenderer = null;
                backgroundQuads = null;
            }
        }

        boolean isVisible(float x, float y) {
            frameBuffer.bind();
            ByteBuffer pixels = BufferUtils.createByteBuffer(4);
            OpenGL.instance().glReadPixels((int) x, (int) y, 1, 1, OpenGL.GL_RGBA, OpenGL.GL_UNSIGNED_BYTE, pixels);
            var alpha = pixels.get(3) & 0xF;
            frameBuffer.unbind(true);
            return alpha > 0;
        }
    }
}
