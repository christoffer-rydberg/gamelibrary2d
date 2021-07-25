package com.gamelibrary2d.demos.networkgame.client.settings;

import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.framework.Window;

public final class Dimensions {
    private static final double MAX_MOBILE_SIZE = 200;

    private static Rectangle buttonSize;
    private static Rectangle inputFieldSize;
    private static Point contentScale;
    private static float defaultOffsetY;

    private Dimensions() {

    }

    public static void create(Window window) {
        contentScale = new Point();
        window.getContentScale(150, contentScale);
        if (window.getPhysicalWindowSize() > MAX_MOBILE_SIZE) {
            contentScale.divide(2f);
        }

        buttonSize = Rectangle.create(40f, 7f).resize(contentScale);
        inputFieldSize = Rectangle.create(40f, 7f).resize(contentScale);
        defaultOffsetY = 2f * contentScale.getY();
    }

    public static float getContentScaleX() {
        return contentScale.getX();
    }

    public static float getContentScaleY() {
        return contentScale.getY();
    }

    public static float getDefaultOffsetY() {
        return defaultOffsetY;
    }

    public static Rectangle getInputFieldSize() {
        return inputFieldSize;
    }

    public static Rectangle getButtonSize() {
        return buttonSize;
    }
}
