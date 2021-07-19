package com.gamelibrary2d.demos.networkgame.client.resources;

import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.io.DynamicByteBuffer;
import com.gamelibrary2d.common.io.Read;
import com.gamelibrary2d.common.io.Write;
import com.gamelibrary2d.demos.networkgame.client.ResourceManager;
import com.gamelibrary2d.demos.networkgame.client.settings.Dimensions;
import com.gamelibrary2d.resources.DefaultFont;
import com.gamelibrary2d.resources.Font;

import java.io.File;
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
        try {
            button = DefaultFont.create(new java.awt.Font("Gabriola", java.awt.Font.BOLD, 36), disposer);
            inputField = DefaultFont.create(new java.awt.Font("Gabriola", java.awt.Font.BOLD, 36), disposer);
            timer = DefaultFont.create(new java.awt.Font("Gabriola", java.awt.Font.BOLD, 72), disposer);

            DataBuffer dataBuffer = new DynamicByteBuffer();

            String path = "C:/Users/Christoffer Rydberg/source/repos/JavaGames/gamelibrary2d/demos/android-demo/app/src/main/assets/fonts";

            dataBuffer.clear();
            ((DefaultFont) button).serialize(dataBuffer);
            dataBuffer.flip();
            Write.bytes(dataBuffer, new File(path + "/button.font"), true);

            dataBuffer.clear();
            ((DefaultFont) inputField).serialize(dataBuffer);
            dataBuffer.flip();
            Write.bytes(dataBuffer, new File(path + "/inputField.font"), true);

            dataBuffer.clear();
            ((DefaultFont) timer).serialize(dataBuffer);
            dataBuffer.flip();
            Write.bytes(dataBuffer, new File(path + "/timer.font"), true);
        } catch (Exception e) {
            System.err.println("Exception while creating/saving fonts. Reading from resource manager");
            button = resourceManager.load("fonts/button.font", stream -> readFont(stream, disposer));
            inputField = resourceManager.load("fonts/inputField.font", stream -> readFont(stream, disposer));
            timer = resourceManager.load("fonts/timer.font", stream -> readFont(stream, disposer));
        }

        fontScale = Dimensions.getContentScaleY() / 3f;
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
