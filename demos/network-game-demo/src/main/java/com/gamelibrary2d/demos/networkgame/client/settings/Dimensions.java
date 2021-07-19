package com.gamelibrary2d.demos.networkgame.client.settings;

import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.framework.Window;

public final class Dimensions {
    private static final double MAX_MOBILE_SIZE = 200;

    private static Rectangle buttonSize;
    private static Rectangle inputFieldSize;
    private static Point contentScale;

    private Dimensions() {

    }

    public static void create(Window window) {
        contentScale = new Point();

        boolean isMobile = window.getPhysicalWindowSize() < MAX_MOBILE_SIZE;
        if (isMobile) {
            window.getContentScale(150, contentScale);
            buttonSize = Rectangle.create(40f, 10f).resize(contentScale);
            inputFieldSize = Rectangle.create(40f, 10f).resize(contentScale);
        } else {
            window.getContentScale(700, contentScale);
            buttonSize = Rectangle.create(100f, 25f).resize(contentScale);
            inputFieldSize = Rectangle.create(100f, 25f).resize(contentScale);
        }
    }

    public static float getContentScaleX() {
        return contentScale.getX();
    }

    public static float getContentScaleY() {
        return contentScale.getY();
    }

    public static float getDefaultOffset() {
        return 10f;
    }

    public static Rectangle getInputFieldSize() {
        return inputFieldSize;
    }

    public static Rectangle getButtonSize() {
        return buttonSize;
    }
}
