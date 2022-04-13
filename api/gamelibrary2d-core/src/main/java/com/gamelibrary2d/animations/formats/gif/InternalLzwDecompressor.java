package com.gamelibrary2d.animations.formats.gif;

import com.gamelibrary2d.common.io.BitInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

class InternalLzwDecompressor {
    private final ByteOrder byteOrder;
    private final int clearCode;
    private final int eoi;
    private final Table table;

    public InternalLzwDecompressor(int initialCodeSize, ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
        clearCode = 1 << initialCodeSize;
        eoi = clearCode + 1;
        table = new Table(initialCodeSize);
    }

    public byte[] decompress(InputStream is, int length) throws IOException {
        BitInputStream input = new BitInputStream(is, byteOrder);
        ByteBuffer output = new ByteBuffer(length);

        table.clear();
        int previousCode = -1;
        while (output.getSize() < length) {
            int code = input.readBits(table.codeSize);
            if (code == eoi) {
                break;
            } else if (code == clearCode) {
                table.clear();
            } else {
                byte[] phrase;
                if (previousCode == clearCode) {
                    phrase = table.getPhrase(code);
                } else if (code < table.codes) {
                    byte[] previousPhrase = table.getPhrase(previousCode);
                    phrase = table.getPhrase(code);
                    table.addPhrase(appendByte(previousPhrase, phrase[0]));
                } else {
                    byte[] previousPhrase = table.getPhrase(previousCode);
                    phrase = appendByte(previousPhrase, previousPhrase[0]);
                    table.addPhrase(phrase);
                }

                output.write(phrase);
            }

            previousCode = code;
        }

        return output.data;
    }

    private static byte[] appendByte(byte[] bytes, byte b) {
        byte[] result = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        result[result.length - 1] = b;
        return result;
    }

    private static class Table {
        private final byte[][] table = new byte[4096][];
        private final int initialCodeSize;
        private int codeSize;
        private int codes = -1;

        public Table(int initialCodeSize) {
            this.initialCodeSize = initialCodeSize;
            int initialPhrases = 1 << initialCodeSize + 2;
            for (int i = 0; i < initialPhrases; ++i) {
                table[i] = new byte[]{(byte) i};
            }
        }

        private byte[] getPhrase(int code) throws IOException {
            if (code < 0 || code >= codes) {
                throw new IOException("Invalid lzw code: " + code);
            }

            return table[code];
        }

        public void addPhrase(byte[] bytes) {
            int limit = 1 << codeSize;
            if (codes < limit) {
                table[codes] = bytes;
                ++codes;
                if (codes == limit && codeSize < 12) {
                    ++codeSize;
                }
            }
        }

        public void clear() {
            codeSize = initialCodeSize + 1;
            codes = (1 << initialCodeSize) + 2;
        }
    }

    private static class ByteBuffer {
        private final byte[] data;
        private int prevIndex = -1;

        public ByteBuffer(int size) {
            this.data = new byte[size];
        }

        private void write(byte[] bytes) {
            for (int i = 0; i < bytes.length; ++i) {
                data[++prevIndex] = bytes[i];
            }
        }

        private int getSize() {
            return prevIndex + 1;
        }
    }
}
