package com.gamelibrary2d.demos.networkgame.client.resources;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.demos.networkgame.client.settings.Dimensions;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.opengl.resources.Quad;
import com.gamelibrary2d.opengl.resources.Surface;

public final class Surfaces {
    private static Surface button;
    private static Surface inputField;

    private Surfaces() {

    }

    public static void create(Disposer disposer) {
        button = Quad.create(Dimensions.getButtonSize(), disposer);
        inputField = Quad.create(Dimensions.getInputFieldSize(), disposer);
    }

    public static Surface button() {
        return button;
    }

    public static Surface inputField() {
        return inputField;
    }

    /**
     * Creates a small as possible quad that covers the specific area while respecting the aspect ratio.
     */
    public static Quad coverArea(Rectangle area, float ratioWidth, float ratioHeight, Disposer disposer) {
        float ratio = ratioWidth / ratioHeight;

        float boundsWidth = area.getWidth();
        float boundsHeight = area.getHeight();

        float actualWidth = Math.max(area.getWidth(), boundsHeight * ratio);
        float actualHeight = Math.max(area.getHeight(), boundsWidth / ratio);

        Rectangle paddedBackgroundBounds = area.pad(
                (actualWidth - boundsWidth) / 2f,
                (actualHeight - boundsHeight) / 2f);

        return Quad.create(paddedBackgroundBounds, disposer);
    }
}
