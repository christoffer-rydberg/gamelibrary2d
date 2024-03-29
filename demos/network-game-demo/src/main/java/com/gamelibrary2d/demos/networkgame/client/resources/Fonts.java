package com.gamelibrary2d.demos.networkgame.client.resources;

import com.gamelibrary2d.demos.networkgame.client.ResourceManager;
import com.gamelibrary2d.demos.networkgame.client.settings.Dimensions;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.io.Read;
import com.gamelibrary2d.text.DefaultFont;
import com.gamelibrary2d.text.Font;

import java.io.IOException;
import java.io.InputStream;

public final class Fonts {
    private static Font button;
    private static Font inputField;
    private static Font timer;
    private static float fontScale;

    private Fonts() {

    }

    private static Font readFont(InputStream is, Disposer disposer) throws IOException {
        DataBuffer bytes = Read.bytes(is);
        bytes.flip();
        return DefaultFont.load(bytes, disposer);
    }

    public static void create(ResourceManager resourceManager, Disposer disposer) throws IOException {
        float buttonFontSize = Dimensions.getContentScaleY() * 5f;
        fontScale = buttonFontSize / 32f;

        button = resourceManager.load(
                "fonts/button.font",
                stream -> readFont(stream, disposer));

        inputField = resourceManager.load(
                "fonts/input_field.font",
                stream -> readFont(stream, disposer));

        timer = resourceManager.load(
                "fonts/timer.font",
                stream -> readFont(stream, disposer));
    }

    public static float getFontScale() {
        return fontScale;
    }

    public static Font button() {
        return button;
    }

    public static Font inputField() {
        return inputField;
    }

    public static Font timer() {
        return timer;
    }
}
