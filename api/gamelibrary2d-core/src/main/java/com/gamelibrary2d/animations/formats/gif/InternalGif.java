package com.gamelibrary2d.animations.formats.gif;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

class InternalGif {
    final InternalHeader header;
    final InternalLogicalScreenDescriptor logicalScreenDescriptor;
    final InternalColorTable globalColorTable;
    final List<InternalGifFrame> frames;

    InternalGif(
            InternalHeader header,
            InternalLogicalScreenDescriptor logicalScreenDescriptor,
            InternalColorTable globalColorTable,
            List<InternalGifFrame> frames) {
        this.header = header;
        this.logicalScreenDescriptor = logicalScreenDescriptor;
        this.globalColorTable = globalColorTable;
        this.frames = frames;
    }

    private static List<InternalGifFrame> readFrames(InputStream is, ByteOrder byteOrder) throws IOException {
        List<InternalGifFrame> frames = new ArrayList<>();

        while (true) {
            InternalGifFrame frame = InternalGifFrame.read(is, byteOrder);
            if (frame != null) {
                frames.add(frame);
            } else {
                break;
            }
        }

        return frames;
    }

    public static InternalGif read(final InputStream is, ByteOrder byteOrder) throws IOException {
        InternalHeader header = InternalHeader.read(is, byteOrder);
        InternalLogicalScreenDescriptor lsd = InternalLogicalScreenDescriptor.read(is, byteOrder);

        InternalColorTable globalColorTable = lsd.globalColorTable
                ? InternalColorTable.read(is, lsd.sizeOfGlobalColorTable)
                : null;

        List<InternalGifFrame> frames = readFrames(is, byteOrder);

        return new InternalGif(header, lsd, globalColorTable, frames);
    }
}
