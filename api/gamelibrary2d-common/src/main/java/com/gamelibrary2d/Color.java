package com.gamelibrary2d;

public class Color {

    public static final Color EMPTY = new Color(0, 0, 0, 0);
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color WHITE = new Color(1, 1, 1);
    public static final Color RED = new Color(1, 0, 0);
    public static final Color LIGHT_CORAL = new Color(240f / 255f, 128f / 255f, 128f / 255f);
    public static final Color LIGHT_GREEN = new Color(144f / 255f, 238f / 255f, 144f / 255f);
    public static final Color GREEN = new Color(0, 1, 0);
    public static final Color BLUE = new Color(0, 0, 1);
    public static final Color SKY_BLUE = new Color(135 / 255f, 206 / 255f, 235 / 255f);
    public static final Color LIGHT_BLUE = new Color(173f / 255f, 216f / 255f, 230f / 255f);
    public static final Color SOFT_BLUE = new Color(171f / 255f, 215f / 255f, 235f / 255f);
    public static final Color SILVER = new Color(192f / 255f, 192f / 255f, 192f / 255f);
    public static final Color SAND = new Color(194f / 255f, 178f / 255f, 128f / 255f);
    public static final Color GOLD = new Color(255f / 255f, 215f / 255f, 0f);
    public static final Color PINK = new Color(255f / 255f, 192 / 255f, 203 / 255f);
    public static final Color YELLOW = new Color(255f / 255f, 255f / 255f, 0);
    public static final Color LIGHT_YELLOW = new Color(255f / 255f, 255f / 255f, 204f / 255f);
    public static final Color ORANGE = new Color(255f / 255f, 165f / 255f, 0);
    public static final Color BROWN = new Color(139f / 255f, 69f / 255f, 19f / 255f);
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    public static final Color LAVENDER = new Color(230f / 255f, 230f / 255f, 250f / 255f);

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

    /**
     * A {@link Color} is represented by float values, where 0 is the least intense and 1 is the most intense (range is not enforced as more intense values could make sense in certain shaders).
     * This factory function helps create a {@link Color} by specifying the corresponding int values in the range 0 to 255.
     */
    public static Color create256(int r, int g, int b) {
        return Color.create256(r, g, b, 0);
    }

    /**
     * A {@link Color} is represented by float values, where 0 is the least intense and 1 is the most intense (range is not enforced as more intense values could make sense in certain shaders).
     * This factory function helps create a {@link Color} by specifying the corresponding int values in the range 0 to 255.
     */
    public static Color create256(int r, int g, int b, int a) {
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
     * @return The approximate R-channel value, typically ranging between 0 and 255 (it's not restricted as more intense values could make sense in certain shaders).
     */
    public int getR256() {
        return Math.round(r * 255f);
    }

    /**
     * @return The G-channel value, typically ranging between 0 and 1 (it's not restricted as more intense values could make sense in certain shaders).
     */
    public float getG() {
        return g;
    }

    /**
     * @return The approximate G-channel value, typically ranging between 0 and 255 (it's not restricted as more intense values could make sense in certain shaders).
     */
    public int getG256() {
        return Math.round(g * 255f);
    }

    /**
     * @return The B-channel value, typically ranging between 0 and 1 (it's not restricted as more intense values could make sense in certain shaders).
     */
    public float getB() {
        return b;
    }

    /**
     * @return The approximate B-channel value, typically ranging between 0 and 255 (it's not restricted as more intense values could make sense in certain shaders).
     */
    public int getB256() {
        return Math.round(b * 255f);
    }

    /**
     * @return The A-channel value, typically ranging between 0 and 1 (it's not restricted as more intense values could make sense in certain shaders).
     */
    public float getA() {
        return a;
    }

    /**
     * @return The approximate A-channel value, typically ranging between 0 and 255 (it's not restricted as more intense values could make sense in certain shaders).
     */
    public int getA256() {
        return Math.round(a * 255f);
    }
}