package com.gamelibrary2d;

public class Color {

    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    public static final Color BLACK = fromBytes(0, 0, 0);
    public static final Color WHITE = fromBytes(255, 255, 255);
    public static final Color RED = fromBytes(255, 0, 0);
    public static final Color LIGHT_RED = fromBytes(255, 102, 102);
    public static final Color DARK_RED = fromBytes(139, 0, 0);
    public static final Color GREEN = fromBytes(0, 255, 0);
    public static final Color LIGHT_GREEN = fromBytes(144, 238, 144);
    public static final Color DARK_GREEN = fromBytes(0, 100, 0);
    public static final Color BLUE = fromBytes(0, 0, 255);
    public static final Color LIGHT_BLUE = fromBytes(173, 216, 230);
    public static final Color DARK_BLUE = fromBytes(0, 0, 139);
    public static final Color YELLOW = fromBytes(255, 255, 0);
    public static final Color LIGHT_YELLOW = fromBytes(255, 255, 153);
    public static final Color DARK_YELLOW = fromBytes(204, 204, 0);
    public static final Color CYAN = fromBytes(0, 255, 255);
    public static final Color LIGHT_CYAN = fromBytes(224, 255, 255);
    public static final Color DARK_CYAN = fromBytes(0, 139, 139);
    public static final Color MAGENTA = fromBytes(255, 0, 255);
    public static final Color LIGHT_MAGENTA = fromBytes(255, 153, 255);
    public static final Color DARK_MAGENTA = fromBytes(139, 0, 139);
    public static final Color ORANGE = fromBytes(255, 165, 0);
    public static final Color LIGHT_ORANGE = fromBytes(255, 200, 102);
    public static final Color DARK_ORANGE = fromBytes(204, 102, 0);
    public static final Color PURPLE = fromBytes(128, 0, 128);
    public static final Color LIGHT_PURPLE = fromBytes(216, 191, 216);
    public static final Color DARK_PURPLE = fromBytes(75, 0, 130);
    public static final Color PINK = fromBytes(255, 192, 203);
    public static final Color LIGHT_PINK = fromBytes(255, 222, 233);
    public static final Color DARK_PINK = fromBytes(231, 84, 128);
    public static final Color BROWN = fromBytes(165, 42, 42);
    public static final Color LIGHT_BROWN = fromBytes(205, 133, 63);
    public static final Color DARK_BROWN = fromBytes(101, 67, 33);
    public static final Color GRAY = fromBytes(128, 128, 128);
    public static final Color LIGHT_GRAY = fromBytes(211, 211, 211);
    public static final Color DARK_GRAY = fromBytes(64, 64, 64);

    public static final Color GOLD = fromBytes(255, 215, 0);
    public static final Color SILVER = fromBytes(192, 192, 192);
    public static final Color CRIMSON = fromBytes(220, 20, 60);
    public static final Color EMERALD = fromBytes(80, 200, 120);
    public static final Color TEAL = fromBytes(0, 128, 128);
    public static final Color VIOLET = fromBytes(138, 43, 226);
    public static final Color NAVY = fromBytes(0, 0, 128);
    public static final Color INDIGO = fromBytes(75, 0, 130);
    public static final Color AMBER = fromBytes(255, 191, 0);
    public static final Color LAVENDER = fromBytes(230, 230, 250);
    public static final Color SAND = fromBytes(194, 178, 128);

    private final float r;
    private final float g;
    private final float b;
    private final float a;

    public Color(Color color, float alpha) {
        this(color.r, color.g, color.b, alpha);
    }

    public Color(float r, float g, float b) {
        this(r, g, b, 1);
    }

    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public static Color fromBytes(int r, int g, int b) {
        return new Color(r/255f, g/255f, b/255f, 1f);
    }

    public static Color fromBytes(int r, int g, int b, int a) {
        return new Color(r/255f, g/255f, b/255f, a/255f);
    }

    public Color add(Color c) {
        return new Color(r + c.r, g + c.g, b + c.b, a + c.a);
    }

    public Color subtract(Color c) {
        return new Color(r - c.r, g - c.g, b - c.b, a - c.a);
    }

    public Color multiply(Color c) {
        return new Color(r * c.r, g * c.g, b * c.b, a * c.a);
    }

    public Color multiply(float factor) {
        return new Color(r * factor, g * factor, b * factor, a * factor);
    }

    public Color divide(Color c) {
        return new Color(r / c.r, g / c.g, b / c.b, a / c.a);
    }

    public Color divide(float divisor) {
        return new Color(r / divisor, g / divisor, b / divisor, a / divisor);
    }

    /**
     * @return The R-channel value, typically ranging between 0 and 1 (it's not restricted as more intense values could make sense in certain shaders).
     */
    public float getR() {
        return r;
    }

    /**
     * @return The G-channel value, typically ranging between 0 and 1 (it's not restricted as more intense values could make sense in certain shaders).
     */
    public float getG() {
        return g;
    }

    /**
     * @return The B-channel value, typically ranging between 0 and 1 (it's not restricted as more intense values could make sense in certain shaders).
     */
    public float getB() {
        return b;
    }

    /**
     * @return The A-channel value, typically ranging between 0 and 1 (it's not restricted as more intense values could make sense in certain shaders).
     */
    public float getA() {
        return a;
    }
}