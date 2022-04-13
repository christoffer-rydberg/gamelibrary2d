package com.gamelibrary2d.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

public class BitInputStream extends InputStream {
    private final static int cacheSize = Integer.BYTES * 8;

    private final InputStream internalStream;
    private final ByteOrder byteOrder;

    private int cache;
    private int bitsInCache;

    public BitInputStream(InputStream internalStream, ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
        this.internalStream = internalStream;
    }

    @Override
    public int read() throws IOException {
        return readBits(8);
    }

    public int readBits(int bits) throws IOException {
        if (bits > cacheSize) {
            throw new IOException(String.format(
                    "Attempting to read %d bits into integer (max bit size = %d).",
                    bits,
                    cacheSize));
        } else if (bits <= 0) {
            throw new IOException(String.format(
                    "Number of bits cannot be zero or negative: %d",
                    bits));
        }

        if (tryEnsureCacheCapacity(bits)) {
            return readFromCache(bits);
        } else if (bitsInCache > 0) {
            return readFromCache(bitsInCache);
        } else {
            return -1;
        }
    }

    private int readFromCache(int bits) {
        bitsInCache -= bits;
        int mask = (1 << bits) - 1;
        int bitCacheMask = (1 << bitsInCache) - 1;

        int result;
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            result = mask & cache;
            cache = (cache >> bits) & bitCacheMask;
        } else {
            result = mask & (cache >> bitsInCache);
            cache &= bitCacheMask;
        }

        return result;
    }

    private boolean tryEnsureCacheCapacity(int bits) throws IOException {
        while (bitsInCache < bits) {
            int nextByte = internalStream.read();
            if (nextByte < 0) {
                return false;
            }

            cache = byteOrder == ByteOrder.LITTLE_ENDIAN
                    ? (nextByte << bitsInCache) | cache
                    : (cache << 8) | nextByte;

            bitsInCache += 8;
        }

        return true;
    }
}
