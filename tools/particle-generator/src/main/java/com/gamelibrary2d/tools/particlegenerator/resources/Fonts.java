package com.gamelibrary2d.tools.particlegenerator.resources;

import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.lwjgl.imaging.FontMetadataFactory;
import com.gamelibrary2d.text.DefaultFont;
import com.gamelibrary2d.text.Font;

public class Fonts {
    private static Font smallFont;
    private static Font defaultFont;
    private static Font largeFont;
    private static Font menuFont;

    public static void create(Disposer disposer) {
        smallFont = DefaultFont.create(
                FontMetadataFactory.create(new java.awt.Font("Gabriola", java.awt.Font.BOLD, 12)),
                disposer);

        defaultFont = DefaultFont.create(
                FontMetadataFactory.create(new java.awt.Font("Gabriola", java.awt.Font.BOLD, 16)),
                disposer);

        largeFont = DefaultFont.create(
                FontMetadataFactory.create(new java.awt.Font("Gabriola", java.awt.Font.BOLD, 20)),
                disposer);

        menuFont = DefaultFont.create(
                FontMetadataFactory.create(new java.awt.Font("Gabriola", java.awt.Font.BOLD, 32)),
                disposer);
    }

    public static Font getSmallFont() {
        return smallFont;
    }

    public static Font getDefaultFont() {
        return defaultFont;
    }

    public static Font getLargeFont() {
        return largeFont;
    }

    public static Font getMenuFont() {
        return menuFont;
    }

}