package com.gamelibrary2d.animations.formats.gif;

import static com.gamelibrary2d.animations.formats.gif.InternalInputStreamExtensions.*;
import java.io.IOException;
import java.io.InputStream;

class InternalColorTable {
    private final byte[] colorTable;

    public InternalColorTable(byte[] colorTable) {
        this.colorTable = colorTable;
    }

    public int getColor(int index) {
        int red = 0xff & colorTable[(index * 3)];
        int green = 0xff & colorTable[(index * 3) + 1];
        int blue = 0xff & colorTable[(index * 3) + 2];
        int alpha = 0xff;
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int pow2(int pow) {
        int result =1;
        for (int i = 0; i < pow; ++i) {
            result *= 2;
        }

        return result;
    }

    public static InternalColorTable read(InputStream is, int tableSize) throws IOException {
        int actualTableSize = 3 * pow2(tableSize + 1);
        return new InternalColorTable(readBytesOrThrow(is, actualTableSize));
    }
}
