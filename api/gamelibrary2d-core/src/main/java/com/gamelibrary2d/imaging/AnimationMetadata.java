package com.gamelibrary2d.imaging;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.denotations.Bounded;
import java.util.List;

public class AnimationMetadata {
    private final List<AnimationFrameMetadata> frames;

    public AnimationMetadata(List<AnimationFrameMetadata> frames) {
        this.frames = frames;
    }

    public List<AnimationFrameMetadata> getFrames() {
        return frames;
    }

    public Rectangle calculateBounds() {
        float xMin = Float.MAX_VALUE, yMin = Float.MAX_VALUE;
        float xMax = Float.MIN_VALUE, yMax = Float.MIN_VALUE;
        for (Bounded frame : frames) {
            Rectangle bounds = frame.getBounds();
            xMin = Math.min(xMin, bounds.getLowerX());
            yMin = Math.min(yMin, bounds.getLowerY());
            xMax = Math.max(xMax, bounds.getUpperX());
            yMax = Math.max(yMax, bounds.getUpperY());
        }

        return new Rectangle(xMin, yMin, xMax, yMax);
    }
}
