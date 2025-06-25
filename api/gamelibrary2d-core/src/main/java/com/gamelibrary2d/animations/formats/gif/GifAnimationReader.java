package com.gamelibrary2d.animations.formats.gif;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.animations.AnimationFrameMetadata;
import com.gamelibrary2d.animations.AnimationMetadata;
import com.gamelibrary2d.animations.AnimationReader;
import com.gamelibrary2d.animations.AnimationFrameBufferHint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class GifAnimationReader implements AnimationReader {
    private static final Rectangle IMAGE_COORDINATES = new Rectangle(0, 0, 1, 1);
    private static final int NO_DISPOSAL_SPECIFIED = 0;
    private static final int DO_NOT_DISPOSE = 1;
    private static final int RESTORE_TO_BACKGROUND = 2;
    private static final int RESTORE_TO_PREVIOUS = 3;

    private static AnimationFrameMetadata createAnimationFrame(
            InternalLogicalScreenDescriptor logicalScreenDescriptor,
            InternalColorTable globalColorTable,
            InternalGifFrame gifFrame) {

        InternalGraphicControlExtension gce = gifFrame.graphicControlExtension;

        float duration;
        AnimationFrameBufferHint bufferHint;
        int transparentColorIndex;
        if (gce != null) {
            duration = gce.delayTime / 100f;

            transparentColorIndex = gce.transparentColor
                    ? gce.transparentColorIndex
                    : -1;

            int disposalMethod = gifFrame.graphicControlExtension.disposalMethod;
            switch (disposalMethod) {
                case NO_DISPOSAL_SPECIFIED:
                case DO_NOT_DISPOSE:
                    bufferHint = AnimationFrameBufferHint.WRITE_TO_BUFFER;
                    break;
                case RESTORE_TO_BACKGROUND:
                    bufferHint = AnimationFrameBufferHint.CLEAR_BUFFER;
                    break;
                case RESTORE_TO_PREVIOUS:
                    bufferHint = AnimationFrameBufferHint.IGNORE;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + disposalMethod);
            }
        } else {
            duration = 0f;
            bufferHint = AnimationFrameBufferHint.CLEAR_BUFFER;
            transparentColorIndex = -1;
        }

        InternalGifImage image = new InternalGifImage(
                gifFrame.imageDescriptor.imageWidth,
                gifFrame.imageDescriptor.imageHeight,
                transparentColorIndex >= 0);

        InternalColorTable colorTable = gifFrame.localColorTable != null
                ? gifFrame.localColorTable
                : globalColorTable;

        int bufferIndex = -1;
        for (int y = 0; y < gifFrame.imageDescriptor.imageHeight; ++y) {
            for (int x = 0; x < gifFrame.imageDescriptor.imageWidth; ++x) {
                int colorIndex = 0xff & gifFrame.imageData.data[++bufferIndex];
                if (colorIndex == transparentColorIndex) {
                    image.setRgb(x, y, 0x00);
                } else {
                    image.setRgb(x, y, colorTable.getColor(colorIndex));
                }
            }
        }

        int offsetX = gifFrame.imageDescriptor.imageLeftPosition;
        int offsetY = logicalScreenDescriptor.logicalScreenHeight - image.getHeight() - gifFrame.imageDescriptor.imageTopPosition;

        return new AnimationFrameMetadata(image, IMAGE_COORDINATES, offsetX, offsetY, duration, bufferHint);
    }

    public AnimationMetadata read(InputStream stream) throws IOException {
        InternalGif gif = InternalGif.read(stream, ByteOrder.LITTLE_ENDIAN);

        List<AnimationFrameMetadata> frames = new ArrayList<>(gif.frames.size());
        for (InternalGifFrame gifFrame : gif.frames) {
            frames.add(createAnimationFrame(
                    gif.logicalScreenDescriptor,
                    gif.globalColorTable,
                    gifFrame));
        }

        return new AnimationMetadata(frames);
    }
}
