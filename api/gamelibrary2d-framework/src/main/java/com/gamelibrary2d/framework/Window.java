package com.gamelibrary2d.framework;

import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.Rectangle;

public interface Window {

    void initialize();

    void setEventListener(WindowEventListener eventListener);

    void show();

    void setTitle(String string);

    String getTitle();

    void pollEvents();

    void render(Renderable content, float alpha);

    void dispose();

    boolean isCloseRequested();

    void focus();

    boolean isFullScreen();

    /**
     * The width of the window in screen coordinates.
     */
    int getWidth();

    /**
     * The height of the window in screen coordinates.
     */
    int getHeight();

    /**
     * The width of the primary monitor in pixels.
     */
    int getMonitorWidth();

    /**
     * The height of the primary monitor in pixels.
     */
    int getMonitorHeight();

    /**
     * The physical width of the primary monitor in millimetres.
     */
    double getPhysicalWidth();

    /**
     * The physical height of the primary monitor in millimetres.
     */
    double getPhysicalHeight();

    /**
     * Provides a content scale - a factor that can be used to scale UI components and keep a reasonable size for
     * different monitors and/or window sizes.
     * <br>
     * <br>
     * Example:
     * <br>
     * Say that you want a component to appear as approximately 100x100 mm on a window with a physical size of
     * 700 mm (the physical length of the diagonal).
     * <br>
     * <br>
     * 1. Create a {@link Rectangle} of size 100x100.
     * <br>
     * <br>
     * 2. Create a content scale with the base size set to 700.
     * <br>
     * <br>
     * 3. Apply the content scale to the rectangle (e.g. by using {@link Rectangle#resize}).
     * <br>
     * <br>
     * <p>
     * The base size is needed as a point of reference. In the above example you have set your reference to 700 mm.
     * By keeping that very same base size when running your game on a different monitor (or in windowed mode with a different window size),
     * your component will scale to fit that screen. For example, if you run the above example (base size = 700) on a monitor with a physical size
     * of 350 mm, the component will appear as approximately 50x50 mm.
     * <br>
     * <br>
     * Since the scale is determined by the length of the window's diagonal, the result might not be desirable for
     * windows with vastly different aspect ratios.
     *
     * @param baseSize The physical length of the window's diagonal in millimetres.
     * @param output   Output for the content scale.
     */
    default void getContentScale(double baseSize, Point output) {
        double scaleFactor = getPhysicalWindowSize() / baseSize;
        double invertedPixelSizeX = 1.0 / getPhysicalPixelWidth();
        double invertedPixelSizeY = 1.0 / getPhysicalPixelHeight();
        output.set((float) (invertedPixelSizeX * scaleFactor), (float) (invertedPixelSizeY * scaleFactor));
    }

    default double getPhysicalPixelWidth() {
        return isFullScreen()
                ? getPhysicalWidth() / getWidth()
                : getPhysicalWidth() / getMonitorWidth();
    }

    default double getPhysicalPixelHeight() {
        return isFullScreen()
                ? getPhysicalHeight() / getHeight()
                : getPhysicalHeight() / getMonitorHeight();
    }

    default double getPhysicalMonitorSize() {
        double physicalWidth = getPhysicalWidth();
        double physicalHeight = getPhysicalHeight();
        return Math.sqrt(physicalWidth * physicalWidth + physicalHeight * physicalHeight);
    }

    default double getPhysicalWindowSize() {
        if (isFullScreen()) {
            return getPhysicalMonitorSize();
        } else {
            double widthFactor = (double) getWidth() / getMonitorWidth();
            double heightFactor = (double) getHeight() / getMonitorHeight();
            double physicalWidth = getPhysicalWidth() * widthFactor;
            double physicalHeight = getPhysicalHeight() * heightFactor;
            return Math.sqrt(physicalWidth * physicalWidth + physicalHeight * physicalHeight);
        }
    }
}
