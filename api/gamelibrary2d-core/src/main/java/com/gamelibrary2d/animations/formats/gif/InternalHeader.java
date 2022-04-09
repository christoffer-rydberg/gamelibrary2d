package com.gamelibrary2d.animations.formats.gif;

import static com.gamelibrary2d.animations.formats.gif.InternalInputStreamExtensions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

class InternalHeader {
    final int signature;
    final int version;

    InternalHeader(int signature, int version) {
        this.signature = signature;
        this.version = version;
    }

    public static InternalHeader read(final InputStream is, ByteOrder byteOrder) throws IOException {
        int signature = read3BytesOrThrow(is, byteOrder);
        int version = read3BytesOrThrow(is, byteOrder);
        return new InternalHeader(signature, version);
    }
}
