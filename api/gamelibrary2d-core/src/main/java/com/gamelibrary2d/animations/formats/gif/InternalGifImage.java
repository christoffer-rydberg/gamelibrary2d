package com.gamelibrary2d.animations.formats.gif;

import com.gamelibrary2d.framework.Image;

class InternalGifImage implements Image {
    private final byte[] data;
    private final int width;
    private final int height;
    private final int channels;

    public InternalGifImage(int width, int height, boolean hasAlpha) {
        this.width = width;
        this.height = height;
        channels = hasAlpha ? 4 : 3;
        data = new byte[width * height * channels];
    }

    private int getIndex(int x, int y) {
        return ((height - 1 - y) * width + x) * channels;
    }

    public void setRgb(int x, int y, int argb) {
        int index = getIndex(x, y);
        data[index] = (byte) ((argb >> 16) & 0xFF);         // R
        data[index + 1] = (byte) ((argb >> 8) & 0xFF);      // G
        data[index + 2] = (byte) (argb & 0xFF);             // B
        if (channels == 4) {
            data[index + 3] = (byte) ((argb >> 24) & 0xFF); // A
        }
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getChannels() {
        return channels;
    }
}
