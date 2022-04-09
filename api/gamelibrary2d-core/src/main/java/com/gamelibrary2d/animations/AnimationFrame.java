package com.gamelibrary2d.animations;

import com.gamelibrary2d.resources.DefaultTexture;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.components.denotations.Bounded;
import com.gamelibrary2d.resources.Quad;
import com.gamelibrary2d.resources.Surface;
import com.gamelibrary2d.resources.Texture;

public class AnimationFrame implements Bounded {
    private final Surface surface;
    private final Texture texture;
    private final boolean restoreBackgroundHint;
    private final boolean renderToBackgroundHint;
    private final float durationHint;

    /**
     * Creates a new instance of {@link AnimationFrame}.
     *
     * @param surface  The frame surface.
     * @param texture  The frame texture.
     * @param duration Sets the {@link AnimationFrame#getDurationHint} field.
     */
    public AnimationFrame(Surface surface, Texture texture, float duration) {
        this(surface, texture, duration, false, false);
    }

    /**
     * Creates a new instance of {@link AnimationFrame}.
     *
     * @param surface            The frame surface.
     * @param texture            The frame texture.
     * @param duration           Sets the {@link AnimationFrame#getDurationHint} field.
     * @param restoreBackground  Sets the {@link AnimationFrame#restoreBackgroundHint} field.
     * @param renderToBackground Sets the {@link AnimationFrame#getRenderToBackgroundHint} field.
     */
    public AnimationFrame(Surface surface, Texture texture, float duration, boolean restoreBackground, boolean renderToBackground) {
        this.surface = surface;
        this.texture = texture;
        this.durationHint = duration;
        this.restoreBackgroundHint = restoreBackground;
        this.renderToBackgroundHint = renderToBackground;
    }

    public static AnimationFrame create(AnimationFrameMetadata metadata, Rectangle bounds, Disposer disposer) {
        Texture texture = DefaultTexture.create(metadata.getImage(), disposer);
        Surface surface = Quad.create(bounds, metadata.getImageCoordinates(), disposer);
        return new AnimationFrame(
                surface,
                texture,
                metadata.getDurationHint(),
                metadata.getRestoreBackgroundHint(),
                metadata.getRenderToBackgroundHint());
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
     * Indicates if the animation background should be restored before rendering this frame.
     */
    public boolean getRestoreBackgroundHint() {
        return restoreBackgroundHint;
    }

    /**
     * Indicates if the animation frame should be rendered to the animation background.
     */
    public boolean getRenderToBackgroundHint() {
        return renderToBackgroundHint;
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
