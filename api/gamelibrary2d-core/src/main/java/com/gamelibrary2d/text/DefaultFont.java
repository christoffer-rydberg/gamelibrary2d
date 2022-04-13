package com.gamelibrary2d.text;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.disposal.DefaultDisposer;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.common.io.DataBuffer;
import com.gamelibrary2d.common.io.Serializable;
import com.gamelibrary2d.framework.DefaultImage;
import com.gamelibrary2d.framework.FontMetadata;
import com.gamelibrary2d.framework.Image;
import com.gamelibrary2d.opengl.ModelMatrix;
import com.gamelibrary2d.opengl.resources.DefaultTexture;
import com.gamelibrary2d.opengl.resources.Quad;
import com.gamelibrary2d.opengl.resources.Surface;
import com.gamelibrary2d.opengl.resources.Texture;
import com.gamelibrary2d.opengl.shaders.ShaderProgram;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultFont implements Font, Serializable {
    private final Texture texture;
    private final Map<Character, Quad> quads;
    private final int ascent;
    private final int descent;
    private final int height;

    public DefaultFont(Texture texture, Map<Character, Quad> quads, int ascent, int descent, int height) {
        this.texture = texture;
        this.quads = Collections.unmodifiableMap(quads);
        this.ascent = ascent;
        this.descent = descent;
        this.height = height;
    }

    public static DefaultFont create(FontMetadata metadata, Disposer disposer) {
        Map<Character, Quad> quads = new HashMap<>();
        for (Character c : metadata.getCharacters().keySet()) {
            FontMetadata.CharacterMetadata charBounds = metadata.getCharacters().get(c);
            Quad quad = Quad.create(charBounds.getBounds(), charBounds.getTextureBounds(), disposer);
            quads.put(c, quad);
        }

        Texture texture = DefaultTexture.create(metadata.getImage(), disposer);

        return new DefaultFont(texture, quads, metadata.getAscent(), metadata.getDescent(), metadata.getFontHeight());
    }

    public static DefaultFont load(DataBuffer buffer, Disposer parentDisposer) {
        int ascent = buffer.getInt();
        int descent = buffer.getInt();
        int height = buffer.getInt();

        Disposer disposer = new DefaultDisposer(parentDisposer);

        int numberOfQuads = buffer.getInt();
        Map<Character, Quad> quads = new HashMap<>();
        for (int i = 0; i < numberOfQuads; ++i) {
            char character = (char) buffer.getInt();
            Rectangle bounds = deserializeRectangle(buffer);
            Rectangle textureBounds = deserializeRectangle(buffer);
            quads.put(character, Quad.create(bounds, textureBounds, disposer));
        }

        int textureWidth = buffer.getInt();
        int textureHeight = buffer.getInt();
        int textureChannels = buffer.getInt();

        byte[] bytes = new byte[buffer.getInt()];
        buffer.get(bytes, 0, bytes.length);

        Image image = new DefaultImage(bytes, textureWidth, textureHeight, textureChannels);

        Texture texture = DefaultTexture.create(image, disposer);

        return new DefaultFont(texture, quads, ascent, descent, height);
    }

    private static Rectangle deserializeRectangle(DataBuffer buffer) {
        return new Rectangle(buffer.getFloat(), buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
    }

    private static void serializeRectangle(Rectangle rectangle, DataBuffer buffer) {
        buffer.putFloat(rectangle.getLowerX());
        buffer.putFloat(rectangle.getLowerY());
        buffer.putFloat(rectangle.getUpperX());
        buffer.putFloat(rectangle.getUpperY());
    }

    @Override
    public void serialize(DataBuffer buffer) {
        buffer.putInt(ascent);
        buffer.putInt(descent);
        buffer.putInt(height);

        buffer.putInt(quads.size());
        for (Character character : quads.keySet()) {
            Quad quad = quads.get(character);
            int charInt = (int) character;
            buffer.putInt(charInt);
            serializeRectangle(quad.getBounds(), buffer);
            serializeRectangle(quad.getTextureBounds(), buffer);
        }

        Image textureImage = texture.loadImage();
        buffer.putInt(textureImage.getWidth());
        buffer.putInt(textureImage.getHeight());
        buffer.putInt(textureImage.getChannels());

        byte[] imageData = textureImage.getData();

        buffer.putInt(imageData.length);
        buffer.put(imageData);
    }

    @Override
    public void render(ShaderProgram shaderProgram, String text, int offset, int length) {
        texture.bind();

        ModelMatrix.instance().pushMatrix();

        for (int i = offset; i < offset + length; ++i) {
            Quad quad = quads.get(text.charAt(i));
            if (quad == null)
                continue;

            shaderProgram.updateModelMatrix();

            quad.render(shaderProgram);

            float width = quad.getBounds().getWidth();

            ModelMatrix.instance().translatef(width, 0, 0);
        }

        ModelMatrix.instance().popMatrix();
    }

    public Texture getTexture() {
        return texture;
    }

    public Map<Character, Quad> getQuads() {
        return quads;
    }

    @Override
    public int getAscent() {
        return ascent;
    }

    @Override
    public int getDescent() {
        return descent;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public float getTextWidth(String text, int offset, int len) {
        float width = 0;
        int end = Math.min(offset + len, text.length());
        for (int i = offset; i < end; ++i) {
            Surface surface = quads.get(text.charAt(i));
            if (surface != null) {
                Rectangle bounds = surface.getBounds();
                width += bounds.getWidth();
            }
        }

        return width;
    }
}