package com.gamelibrary2d.animations;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Bounded;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.opengl.resources.DefaultTexture;
import com.gamelibrary2d.opengl.resources.Quad;
import com.gamelibrary2d.opengl.resources.Surface;
import com.gamelibrary2d.opengl.resources.Texture;

public class AnimationFrame implements Bounded {
    private final Surface surface;
    private final Texture texture;
    private final AnimationFrameBufferHint bufferHint;
    private final float durationHint;

    /**
     * Creates a new instance of {@link AnimationFrame}.
     *
     * @param surface  The frame surface.
     * @param texture  The frame texture.
     * @param duration Sets the {@link AnimationFrame#getDurationHint} field.
     */
    public AnimationFrame(Surface surface, Texture texture, float duration) {
        this(surface, texture, duration, AnimationFrameBufferHint.CLEAR_BUFFER);
    }

    /**
     * Creates a new instance of {@link AnimationFrame}.
     *
     * @param surface            The frame surface.
     * @param texture            The frame texture.
     * @param duration           Sets the {@link AnimationFrame#getDurationHint} field.
     * @param bufferHint         Sets the {@link AnimationFrame#bufferHint} field.
     */
    public AnimationFrame(Surface surface, Texture texture, float duration, AnimationFrameBufferHint bufferHint) {
        this.surface = surface;
        this.texture = texture;
        this.durationHint = duration;
        this.bufferHint = bufferHint;
    }

    public static AnimationFrame create(AnimationFrameMetadata metadata, Rectangle bounds, Disposer disposer) {
        Texture texture = DefaultTexture.create(metadata.getImage(), disposer);
        Surface surface = Quad.create(bounds, metadata.getImageCoordinates(), disposer);
        return new AnimationFrame(
                surface,
                texture,
                metadata.getDurationHint(),
                metadata.getBufferHint());
    }

    /**
     * The frame surface.
     */
    public Surface getSurface() {
        return surface;
    }

    /**
     * The frame texture.
     */
    public Texture getTexture() {
        return texture;
    }

    /**
     * Indicates what to do with the rendering buffer after rendering this frame.
     */
    public AnimationFrameBufferHint getBufferHint() {
        return bufferHint;
    }

    /**
     * Indicates the intended duration for this frame.
     */
    public float getDurationHint() {
        return durationHint;
    }

    @Override
    public Rectangle getBounds() {
        return surface.getBounds();
    }
}
