package com.gamelibrary2d.animations.formats.gif;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import static com.gamelibrary2d.animations.formats.gif.InternalInputStreamExtensions.read2BytesOrThrow;
import static com.gamelibrary2d.animations.formats.gif.InternalInputStreamExtensions.readOrThrow;

class InternalLogicalScreenDescriptor {
    final int logicalScreenWidth;
    final int logicalScreenHeight;
    final int packedFields;
    final int backgroundColorIndex;
    final int pixelAspectRatio;
    final boolean globalColorTable;
    final int colorResolution;
    final boolean sort;
    final int sizeOfGlobalColorTable;

    InternalLogicalScreenDescriptor(
            int logicalScreenWidth,
            int logicalScreenHeight,
            int packedFields,
            int backgroundColorIndex,
            int pixelAspectRatio,
            boolean globalColorTable,
            int colorResolution,
            boolean sort,
            int sizeOfGlobalColorTable) {
        this.logicalScreenWidth = logicalScreenWidth;
        this.logicalScreenHeight = logicalScreenHeight;
        this.packedFields = packedFields;
        this.backgroundColorIndex = backgroundColorIndex;
        this.pixelAspectRatio = pixelAspectRatio;
        this.globalColorTable = globalColorTable;
        this.colorResolution = colorResolution;
        this.sort = sort;
        this.sizeOfGlobalColorTable = sizeOfGlobalColorTable;
    }

    public static InternalLogicalScreenDescriptor read(final InputStream is, ByteOrder byteOrder) throws IOException {
        final int logicalScreenWidth = read2BytesOrThrow(is, byteOrder);
        final int logicalScreenHeight = read2BytesOrThrow(is,byteOrder);

        final int packedFields = readOrThrow(is);
        final int backgroundColorIndex = readOrThrow(is);
        final int pixelAspectRatio = readOrThrow(is);

        final boolean globalColorTable = ((packedFields & 128) > 0);
        final byte colorResolution = (byte) ((packedFields >> 4) & 7);
        final boolean sort = ((packedFields & 8) > 0);
        final byte sizeofGlobalColorTable = (byte) (packedFields & 7);

        return new InternalLogicalScreenDescriptor(
                logicalScreenWidth,
                logicalScreenHeight,
                packedFields,
                backgroundColorIndex,
                pixelAspectRatio,
                globalColorTable,
                colorResolution,
                sort,
                sizeofGlobalColorTable);
    }
}
