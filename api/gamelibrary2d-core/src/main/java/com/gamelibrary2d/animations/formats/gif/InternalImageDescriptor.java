package com.gamelibrary2d.animations.formats.gif;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import static com.gamelibrary2d.animations.formats.gif.InternalInputStreamExtensions.read2BytesOrThrow;
import static com.gamelibrary2d.animations.formats.gif.InternalInputStreamExtensions.readOrThrow;

class InternalImageDescriptor {
    final int imageLeftPosition;
    final int imageTopPosition;
    final int imageWidth;
    final int imageHeight;
    final boolean localColorTableFlag;
    final boolean interlaceFlag;
    final boolean sortFlag;
    final byte sizeOfLocalColorTable;

    InternalImageDescriptor(int imageLeftPosition,
                            int imageTopPosition,
                            int imageWidth,
                            int imageHeight,
                            boolean localColorTableFlag,
                            boolean interlaceFlag,
                            boolean sortFlag,
                            byte sizeofLocalColorTable) {
        this.imageLeftPosition = imageLeftPosition;
        this.imageTopPosition = imageTopPosition;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.localColorTableFlag = localColorTableFlag;
        this.interlaceFlag = interlaceFlag;
        this.sortFlag = sortFlag;
        this.sizeOfLocalColorTable = sizeofLocalColorTable;
    }

    public static InternalImageDescriptor read(InputStream is, ByteOrder byteOrder) throws IOException {
        int imageLeftPosition = read2BytesOrThrow(is, byteOrder);
        int imageTopPosition = read2BytesOrThrow(is, byteOrder);
        int imageWidth = read2BytesOrThrow(is, byteOrder);
        int imageHeight = read2BytesOrThrow(is, byteOrder);
        int packedFields = readOrThrow(is);

        boolean localColorTableFlag = (((packedFields >> 7) & 1) > 0);
        boolean interlaceFlag = (((packedFields >> 6) & 1) > 0);
        boolean sortFlag = (((packedFields >> 5) & 1) > 0);
        byte sizeOfLocalColorTable = (byte) (packedFields & 7);

        return new InternalImageDescriptor(
                imageLeftPosition,
                imageTopPosition,
                imageWidth,
                imageHeight,
                localColorTableFlag,
                interlaceFlag,
                sortFlag,
                sizeOfLocalColorTable);
    }
}
