package com.gamelibrary2d.tools.fontgenerator;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.components.frames.AbstractFrame;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.io.DataBuffer;
import com.gamelibrary2d.io.DynamicByteBuffer;
import com.gamelibrary2d.io.Write;
import com.gamelibrary2d.lwjgl.imaging.FontMetadataFactory;
import com.gamelibrary2d.text.DefaultFont;

import java.io.File;
import java.io.IOException;

public class FontFrame extends AbstractFrame {

    protected FontFrame(Disposer parentDisposer) {
        super(parentDisposer);
    }

    @Override
    protected void onBegin() {
        DefaultFont defaultFont = createFont(new java.awt.Font("Gabriola", java.awt.Font.BOLD, 48));
        DefaultFont mediumFont = createFont(new java.awt.Font("Gabriola", java.awt.Font.BOLD, 48));
        DefaultFont largeFont = createFont(new java.awt.Font("Gabriola", java.awt.Font.BOLD, 96));
        DefaultFont questionFont = createFont(new java.awt.Font("Gabriola", java.awt.Font.BOLD, 48));

        try {
            saveFont(defaultFont, "default");
            saveFont(mediumFont, "medium");
            saveFont(largeFont, "large");
            saveFont(questionFont, "question");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        super.setBackgroundColor(Color.PINK);
    }

    private DefaultFont createFont(java.awt.Font font) {
        return DefaultFont.create(
            FontMetadataFactory.create(font),
            this);
    }

    private void saveFont(DefaultFont font, String name) throws IOException {
        DataBuffer buffer = new DynamicByteBuffer();
        font.serialize(buffer);
        buffer.flip();

        String path = ".\\Fonts";
        File file = new File(String.format("%s\\%s.font", path, name));
        Write.bytes(buffer, file, true);
    }

    @Override
    protected void onEnd() {

    }

    @Override
    protected void onDispose() {

    }
}
