package com.gamelibrary2d.opengl.resources;

public final class PixelParser {
    private PixelParser() {}

    public static int getR(int pixel) {
        return pixel & 0xFF000000;
    }

    public static int getG(int pixel) {
        return pixel & 0x00FF0000;
    }

    public static int getB(int pixel) {
        return pixel & 0x0000FF00;
    }
    public static int getA(int pixel) {
        return pixel & 0x000000FF;
    }
}
