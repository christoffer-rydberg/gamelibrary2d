package com.gamelibrary2d.imaging;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.denotations.Bounded;
import com.gamelibrary2d.framework.Image;

public class AnimationFrameMetadata implements Bounded {
    private final Image img;
    private final Rectangle bounds;
    private final Rectangle imageCoordinates;
    private final float durationHint;
    private final boolean restoreBackgroundHint;
    private final boolean renderToBackgroundHint;

    /**
     * Creates a new instance of {@link AnimationFrameMetadata}.
     *
     * @param img                The frame image.
     * @param imageCoordinates   The image coordinates used for this frame.
     * @param offsetX            The offset along the X axis.
     * @param offsetY            The offset along the Y axis.
     * @param duration           Sets the {@link AnimationFrameMetadata#getDurationHint} field.
     * @param restoreBackground  Sets the {@link AnimationFrameMetadata#restoreBackgroundHint} field.
     * @param renderToBackground Sets the {@link AnimationFrameMetadata#getRenderToBackgroundHint} field.
     */
    public AnimationFrameMetadata(
            Image img,
            Rectangle imageCoordinates,
            float offsetX,
            float offsetY,
            float duration,
            boolean restoreBackground,
            boolean renderToBackground) {
        this.img = img;
        this.imageCoordinates = imageCoordinates;
        this.durationHint = duration;
        this.restoreBackgroundHint = restoreBackground;
        this.renderToBackgroundHint = renderToBackground;
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
        return bounds;
    }

    public Rectangle getImageCoordinates() {
        return imageCoordinates;
    }
}
