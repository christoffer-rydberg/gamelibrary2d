package com.gamelibrary2d.animations.formats.gif;

import static com.gamelibrary2d.animations.formats.gif.InternalInputStreamExtensions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

class InternalImageData {
    final byte[] data;

    InternalImageData(byte[] data) {
        this.data = data;
    }

    private static byte[] readLzw(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        while (true) {
            int blockSize = 0xff & readOrThrow(is);
            byte[] bytes = readBytesOrThrow(is, blockSize);
            if (bytes.length < 1) {
                break;
            }
            out.write(bytes);
        }

        return out.toByteArray();
    }

    public static InternalImageData read(InputStream is, int expectedLength, ByteOrder byteOrder) throws IOException {
        int lzwMinimumCodeSize = is.read();
        byte[] lzwImageData = readLzw(is);

        InternalLzwDecompressor myLzwDecompressor = new InternalLzwDecompressor(
                lzwMinimumCodeSize,
                byteOrder);

        byte[] imageData = myLzwDecompressor.decompress(
                new ByteArrayInputStream(lzwImageData),
                expectedLength);

        return new InternalImageData(imageData);
    }
}
