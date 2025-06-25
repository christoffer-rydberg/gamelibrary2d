package com.gamelibrary2d.animations;

import com.gamelibrary2d.imaging.Image;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Bounded;

public class AnimationFrameMetadata implements Bounded {
    private final Image img;
    private final Rectangle bounds;
    private final Rectangle imageCoordinates;
    private final float durationHint;
    private final AnimationFrameBufferHint bufferHint;

    /**
     * Creates a new instance of {@link AnimationFrameMetadata}.
     *
     * @param img                The frame image.
     * @param imageCoordinates   The image coordinates used for this frame.
     * @param offsetX            The offset along the X axis.
     * @param offsetY            The offset along the Y axis.
     * @param duration           Sets the {@link AnimationFrameMetadata#getDurationHint} field.
     * @param bufferHint         Sets the {@link AnimationFrameMetadata#bufferHint} field.
     */
    public AnimationFrameMetadata(
            Image img,
            Rectangle imageCoordinates,
            float offsetX,
            float offsetY,
            float duration,
            AnimationFrameBufferHint bufferHint) {
        this.img = img;
        this.imageCoordinates = imageCoordinates;
        this.durationHint = duration;
        this.bufferHint = bufferHint;
        this.bounds = new Rectangle(
                offsetX,
                offsetY,
                offsetX + img.getWidth(),
                offsetY + img.getHeight()
        );
    }

    /**
     * The frame image.
     */
    public Image getImage() {
        return img;
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
        return bounds;
    }

    public Rectangle getImageCoordinates() {
        return imageCoordinates;
    }
}
