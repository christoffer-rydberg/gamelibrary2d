package com.gamelibrary2d.imaging;

import com.gamelibrary2d.Rectangle;

import java.util.Map;

public class FontMetadata {
    private final Image image;
    private final Map<Character, CharacterMetadata> characters;
    private final int ascent;
    private final int descent;
    private final int fontHeight;

    public FontMetadata(Image image, Map<Character, CharacterMetadata> characters, int ascent, int descent, int height) {
        this.image = image;
        this.characters = characters;
        this.ascent = ascent;
        this.descent = descent;
        this.fontHeight = height;
    }

    public Map<Character, CharacterMetadata> getCharacters() {
        return characters;
    }

    public int getAscent() {
        return ascent;
    }

    public int getDescent() {
        return descent;
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public Image getImage() {
        return image;
    }

    public static class CharacterMetadata {
        private final Rectangle bounds;
        private final Rectangle textureBounds;

        public CharacterMetadata(Rectangle bounds, Rectangle textureBounds) {
            this.bounds = bounds;
            this.textureBounds = textureBounds;
        }

        public Rectangle getBounds() {
            return bounds;
        }

        public Rectangle getTextureBounds() {
            return textureBounds;
        }
    }
}
