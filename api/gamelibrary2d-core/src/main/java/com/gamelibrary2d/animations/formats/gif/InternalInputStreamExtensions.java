package com.gamelibrary2d.animations.formats.gif;

import java.io.*;
import java.nio.ByteOrder;

final class InternalInputStreamExtensions {

    private InternalInputStreamExtensions() {

    }

    public static int readOrThrow(InputStream is) throws IOException {
        return readOrThrow(is, "Unexpected end of stream");
    }

    public static int readOrThrow(InputStream is, String errorMessage) throws IOException {
        int result = is.read();
        if (result < 0) {
            throw new IOException(errorMessage);
        }

        return result;
    }

    public static byte[] readBytesOrThrow(InputStream is, int length) throws IOException {
        return readBytesOrThrow(is, length, "Unexpected end of stream");
    }

    public static byte[] readBytesOrThrow(InputStream is, int length, String errorMessage) throws IOException {
        if (length < 0) {
            throw new IOException(String.format("Invalid length: %d", length));
        }

        byte[] bytes = new byte[length];

        int read = 0;
        while (read < length) {
            final int count = is.read(bytes, read, length - read);
            if (count < 0) {
                throw new IOException(errorMessage);
            }

            read += count;
        }

        return bytes;
    }

    public static int read2BytesOrThrow(final InputStream is, final ByteOrder byteOrder) throws IOException {
        return read2BytesOrThrow(is, byteOrder, "Unexpected end of stream");
    }

    public static int read2BytesOrThrow(final InputStream is, final ByteOrder byteOrder, final String errorMessage) throws IOException {
        final int byte0 = is.read();
        final int byte1 = is.read();
        if ((byte0 | byte1) < 0) {
            throw new IOException(errorMessage);
        }

        final int result;
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            result = (byte1 << 8) | byte0;
        } else {
            result = (byte0 << 8) | byte1;
        }

        return result;
    }

    public static int read3BytesOrThrow(final InputStream is, final ByteOrder byteOrder) throws IOException {
        return read3BytesOrThrow(is, byteOrder, "Unexpected end of stream");
    }

    public static int read3BytesOrThrow(final InputStream is, final ByteOrder byteOrder, final String errorMessage) throws IOException {
        final int byte0 = is.read();
        final int byte1 = is.read();
        final int byte2 = is.read();
        if ((byte0 | byte1 | byte2) < 0) {
            throw new IOException(errorMessage);
        }

        final int result;
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            result = (byte2 << 16) | (byte1 << 8) | byte0;
        } else {
            result = (byte0 << 16) | (byte1 << 8) | byte2;
        }

        return result;
    }
}
