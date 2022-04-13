package com.gamelibrary2d.framework.lwjgl;

import com.gamelibrary2d.framework.FontMetadata;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class FontMetadataFactory {

    public static FontMetadata create(java.awt.Font font) {
        FontMetrics fontMetrics = createFontMetrics(font);
        BufferedImage[] charImages = createCharImages(fontMetrics);

        final int padding = 1;
        int imageWidth = 0;
        int imageHeight = fontMetrics.getAscent() + fontMetrics.getDescent();
        for (BufferedImage image : charImages) {
            BufferedImage charImage = image;
            if (charImage == null) {
                charImage = charImages['_'];
            }
            imageWidth += charImage.getWidth() + padding;
        }

        BufferedImage textureImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = textureImage.createGraphics();

        int ascent = fontMetrics.getAscent();
        int descent = fontMetrics.getDescent();
        int height = ascent + descent;

        int currentTextureWidth = 0;
        Map<Character, FontMetadata.CharacterMetadata> quads = new HashMap<>();
        for (int i = 0; i < charImages.length; ++i) {
            BufferedImage charImage = charImages[i];
            if (charImage == null) {
                charImage = charImages['_'];
            }

            int charWidth = charImage.getWidth();
            int charHeight = charImage.getHeight();

            graphics.drawImage(charImage, currentTextureWidth, 0, null);

            float textureStart = (float) currentTextureWidth / imageWidth;
            currentTextureWidth += charWidth;
            float textureEnd = (float) currentTextureWidth / imageWidth;
            currentTextureWidth += padding;

            FontMetadata.CharacterMetadata quad = new FontMetadata.CharacterMetadata(
                    new com.gamelibrary2d.common.Rectangle(0, -descent, charWidth, ascent),
                    new com.gamelibrary2d.common.Rectangle(textureStart, 0, textureEnd, (float) charHeight / imageHeight));

            quads.put((char) i, quad);
        }

        com.gamelibrary2d.framework.Image image = new BufferedImageParser().parse(textureImage);

        return new FontMetadata(image, quads, ascent, descent, height);
    }

    private static BufferedImage[] createCharImages(FontMetrics fontMetrics) {
        final int size = 256 + 8;
        BufferedImage[] charImages = new BufferedImage[size];

        // Skip control codes (ASCII 0 to 31)
        for (int i = 32; i < size; i++) {
            // ASCII 127 is a DEL control code that we can skip
            if (i != 127) {
                BufferedImage charImage = createCharImage(fontMetrics, (char) i);
                if (charImage != null) {
                    charImages[i] = charImage;
                }
            }
        }

        return charImages;
    }

    private static BufferedImage createCharImage(FontMetrics fontMetrics, char c) {
        int charWidth = fontMetrics.charWidth(c);
        if (charWidth == 0) {
            return null;
        }

        int ascent = fontMetrics.getAscent();
        int descent = fontMetrics.getDescent();
        int charHeight = ascent + descent;

        BufferedImage image = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createFontGraphics(image, fontMetrics.getFont());
        g.setPaint(Color.WHITE);
        g.drawString(String.valueOf(c), 0, ascent);
        g.dispose();

        return image;
    }

    private static FontMetrics createFontMetrics(java.awt.Font font) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createFontGraphics(image, font);
        FontMetrics fontMetrics = g.getFontMetrics();
        g.dispose();
        return fontMetrics;
    }

    private static Graphics2D createFontGraphics(BufferedImage image, java.awt.Font font) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(font);
        return g;
    }
}
