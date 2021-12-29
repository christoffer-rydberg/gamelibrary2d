package com.gamelibrary2d.resources;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.components.denotations.Bounded;
import com.gamelibrary2d.imaging.AnimationFrameMetadata;
import com.gamelibrary2d.imaging.AnimationMetadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Animation implements Bounded {
    private final List<AnimationFrame> frames;
    private final float[] timeIndex;
    private final Rectangle bounds;

    public Animation(Iterable<AnimationFrame> frames) {
        this.frames = toReadOnlyList(frames);
        this.bounds = calculateBounds(this.frames);
        this.timeIndex = createTimeIndex(this.frames);
    }

    private static List<AnimationFrame> toReadOnlyList(Iterable<AnimationFrame> frames) {
        List<AnimationFrame> result = new ArrayList<>();
        frames.forEach(result::add);
        return Collections.unmodifiableList(result);
    }

    public static Animation create(AnimationMetadata metadata, Rectangle size, Disposer disposer) {
        Rectangle bounds = calculateBounds(metadata.getFrames());

        float scaleX = size.getWidth() / bounds.getWidth();
        float scaleY = size.getHeight() / bounds.getHeight();

        float offsetX = size.getLowerX() - (bounds.getLowerX() * scaleX);
        float offsetY = size.getLowerY() - (bounds.getLowerY() * scaleY);

        return create(metadata, scaleX, scaleY, offsetX, offsetY, disposer);
    }

    private static Animation create(AnimationMetadata metadata, float scaleX, float scaleY, float offsetX, float offsetY, Disposer disposer) {
        List<AnimationFrame> frames = new ArrayList<>(metadata.getFrames().size());
        for (AnimationFrameMetadata frameMetadata : metadata.getFrames()) {
            Rectangle frameBounds = frameMetadata.getBounds()
                    .resize(scaleX, scaleY)
                    .move(offsetX, offsetY);

            frames.add(AnimationFrame.create(frameMetadata, frameBounds, disposer));
        }

        return new Animation(frames);
    }

    private static <T extends Bounded> Rectangle calculateBounds(Collection<T> frames) {
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

    private static float[] createTimeIndex(List<AnimationFrame> frames) {
        float[] index = new float[frames.size()];
        float duration = 0f;
        for (int i = 0; i < index.length; ++i) {
            AnimationFrame frame = frames.get(i);
            duration += Math.max(0f, frame.getDurationHint());
            index[i] = duration;
        }

        return index;
    }

    public float getDuration() {
        return timeIndex[timeIndex.length - 1];
    }

    public boolean isFrameActive(int frameIndex, float animationTime) {
        float frameStart = frameIndex == 0 ? 0 : timeIndex[frameIndex - 1];
        float frameEnd = timeIndex[frameIndex];
        return animationTime >= frameStart && animationTime < frameEnd;
    }

    public int getFrameIndex(float time) {
        int start = 0, end = timeIndex.length;
        while (end - start > 1) {
            int index = (start + end) / 2;
            float frameStart = timeIndex[index - 1];
            float frameEnd = timeIndex[index];
            if (time >= frameEnd) {
                start = index;
            } else if (time < frameStart) {
                end = index;
            } else {
                return index;
            }
        }

        return start;
    }

    public List<AnimationFrame> getFrames() {
        return frames;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    public AnimationFrame getFrame(int index) {
        return frames.get(index);
    }
}
