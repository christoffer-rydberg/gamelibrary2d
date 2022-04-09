package com.gamelibrary2d.animations.formats.gif;

import static com.gamelibrary2d.animations.formats.gif.InternalInputStreamExtensions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

class InternalGraphicControlExtension {
    final int blockSize;
    final int disposalMethod;
    final int delayTime;
    final boolean transparentColor;
    final int transparentColorIndex;
    final int blockTerminator;

    InternalGraphicControlExtension(
            int blockSize,
            int disposalMethod,
            int delayTime,
            boolean transparentColor,
            int transparentColorIndex,
            int blockTerminator) {
        this.blockSize = blockSize;
        this.disposalMethod = disposalMethod;
        this.transparentColor = transparentColor;
        this.delayTime = delayTime;
        this.transparentColorIndex = transparentColorIndex;
        this.blockTerminator = blockTerminator;
    }

    public static InternalGraphicControlExtension read(InputStream is, ByteOrder byteOrder) throws IOException {
        int blockSize = readOrThrow(is);
        int packedFields = readOrThrow(is);

        int disposalMethod = (packedFields & 0x1c) >> 2;
        boolean transparentColor = (packedFields & 1) != 0;

        int delayTime = read2BytesOrThrow(is, byteOrder);
        int transparentColorIndex = 0xff & readOrThrow(is);

        int blockTerminator = readOrThrow(is);

        return new InternalGraphicControlExtension(
                blockSize,
                disposalMethod,
                delayTime,
                transparentColor,
                transparentColorIndex,
                blockTerminator);
    }
}
