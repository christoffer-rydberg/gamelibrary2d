package com.gamelibrary2d.demos.networkgame.client.settings;

import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.framework.Window;

public class Dimensions {
    private final double MAX_MOBILE_SIZE = 200;

    private final Rectangle buttonSize;
    private final Rectangle inputFieldSize;

    public Dimensions(Window window) {
        Point contentScale = new Point();

        boolean isMobile = window.getPhysicalWindowSize() < MAX_MOBILE_SIZE;
        if (isMobile) {
            window.getContentScale(150, contentScale);
            buttonSize = Rectangle.create(40f, 10f).resize(contentScale);
            inputFieldSize = Rectangle.create(40f, 10f).resize(contentScale);
        } else {
            window.getContentScale(700, contentScale);
            buttonSize = Rectangle.create(100f, 15f).resize(contentScale);
            inputFieldSize = Rectangle.create(100f, 15f).resize(contentScale);
        }
    }

    public Rectangle getInputFieldSize() {
        return inputFieldSize;
    }

    public Rectangle getButtonSize() {
        return buttonSize;
    }
}
