package com.gamelibrary2d.text;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.opengl.ModelMatrix;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Label implements ContentRenderer {
    private final DefaultTextRenderer textRenderer;
    private final List<TextChangedListener> textChangedListeners = new CopyOnWriteArrayList<>();

    private String text = "";
    private float rowHeight;
    private float maxWidth = Float.MAX_VALUE;
    private float maxHeight = Float.MAX_VALUE;
    private Renderable background;

    public Label() {
        this.textRenderer = new DefaultTextRenderer();
    }

    public Label(Font font) {
        this.textRenderer = new DefaultTextRenderer(font);
    }

    public Label(Font font, String text) {
        this(font);
        this.text = text;
    }

    private static boolean isNewLine(String text, int index) {
        return getNewLineSize(text, index) > 0;
    }

    private static int getNewLineSize(String text, int index) {
        if (text.charAt(index) == '\n') {
            return 1;
        } else if (text.charAt(index) == '\r') {
            int next = index + 1;
            return next < text.length() && text.charAt(next) == '\n' ? 2 : 1;
        } else {
            return 0;
        }
    }

    public Renderable getBackground() {
        return background;
    }

    public void setBackground(Renderable background) {
        this.background = background;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        String oldText = this.text;
        if (!text.equals(oldText)) {
            this.text = text;
            for (TextChangedListener listener : textChangedListeners) {
                listener.onTextChanged(oldText, text);
            }
        }
    }

    public void addTextChangedListener(TextChangedListener listener) {
        textChangedListeners.add(listener);
    }

    public void removeTextChangedListener(TextChangedListener listener) {
        textChangedListeners.remove(listener);
    }

    public float getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(float maxWidth) {
        this.maxWidth = maxWidth;
    }

    public float getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(float maxHeight) {
        this.maxHeight = maxHeight;
    }

    public float getRowHeight() {
        return rowHeight;
    }

    public void setRowHeight(float rowHeight) {
        this.rowHeight = rowHeight;
    }

    @Override
    public void render(float alpha) {
        if (background != null) {
            background.render(alpha);
        }

        if (text != null) {
            render(alpha, 0, text.length());
        }
    }

    /**
     * Renders the label with the specified text offset and length.
     *
     * @param alpha  The alpha.
     * @param offset The text offset.
     * @param len    The text length.
     * @return Text offset where rendering stopped, i.e. the first index that wasn't rendered.
     */
    public int render(float alpha, int offset, int len) {
        ModelMatrix.instance().pushMatrix();
        int index = iterateRows(offset, len, true, alpha, null);
        ModelMatrix.instance().popMatrix();
        return index;
    }

    public Rectangle calculateBounds() {
        TextBoundsOutput output = new TextBoundsOutput();
        iterateRows(0, text.length(), false, 1f, output);
        return output.bounds;
    }

    private int iterateRows(int offset, int len, boolean render, float alpha, TextBoundsOutput output) {
        final Font font = getFont();
        final int rowCount = (int) (maxHeight / font.getHeight());
        final float rowHeight = this.rowHeight > 0 ? this.rowHeight : getFont().getHeight();

        int currentRow = 0;
        int rowStart = offset;
        int rowEnd = offset;
        int end = len + offset;
        while (rowStart < end && currentRow < rowCount) {
            rowEnd = Math.min(getRowEnd(text, rowStart, maxWidth), end);

            boolean endOfText = rowEnd == end;
            int newLineSize = endOfText ? 0 : getNewLineSize(text, rowEnd);
            boolean newLineSeparatorFound = newLineSize > 0;
            if (!endOfText && !newLineSeparatorFound) {
                // Back up to avoid cutting words
                while (text.charAt(rowEnd) != ' ') {
                    --rowEnd;
                }
            }

            int rowLength = rowEnd - rowStart;

            if (output != null) {
                output.expandBounds(textRenderer.calculateBounds(text, rowStart, rowLength).move(0, -currentRow * rowHeight));
            }

            if (render) {
                textRenderer.render(alpha, text, rowStart, rowLength);
                ModelMatrix.instance().translatef(0, -rowHeight, 0);
            }

            rowStart = newLineSeparatorFound ? rowEnd + newLineSize : consumeSeparators(text, rowEnd, end);

            rowEnd = rowStart;

            ++currentRow;
        }

        return rowEnd;
    }

    private int consumeSeparators(String text, int index, int len) {
        while (index < len && text.charAt(index) == ' ')
            ++index;
        return index;
    }

    private int getRowEnd(String text, int startIndex, float pixelWidth) {
        final int length = text.length();
        while (startIndex < length && pixelWidth > 0) {
            if (isNewLine(text, startIndex)) {
                break;
            }

            Font font = getFont();
            pixelWidth -= font.getTextWidth(text, startIndex, 1);
            if (pixelWidth > 0) {
                ++startIndex;
            }
        }

        return startIndex;
    }

    public Font getFont() {
        return textRenderer.getFont();
    }

    public void setFont(Font font) {
        textRenderer.setFont(font);
    }

    @Override
    public Rectangle getBounds() {
        return Rectangle.EMPTY;
    }

    @Override
    public float getShaderParameter(int index) {
        return textRenderer.getShaderParameter(index);
    }

    public HorizontalTextAlignment getHorizontalAlignment() {
        return textRenderer.getHorizontalAlignment();
    }

    public void setHorizontalAlignment(HorizontalTextAlignment horizontalAlignment) {
        textRenderer.setHorizontalAlignment(horizontalAlignment);
    }

    public VerticalTextAlignment getVerticalAlignment() {
        return textRenderer.getVerticalAlignment();
    }

    public void setVerticalAlignment(VerticalTextAlignment verticalAlignment) {
        textRenderer.setVerticalAlignment(verticalAlignment);
    }

    public void setAlignment(HorizontalTextAlignment horizontal, VerticalTextAlignment vertical) {
        setHorizontalAlignment(horizontal);
        setVerticalAlignment(vertical);
    }

    @Override
    public void setShaderParameter(int index, float value) {
        textRenderer.setShaderParameter(index, value);
    }

    private static class TextBoundsOutput {
        private Rectangle bounds;

        void expandBounds(Rectangle bounds) {
            this.bounds = this.bounds == null
                    ? bounds
                    : this.bounds.add(bounds);
        }
    }

    public interface TextChangedListener {
        void onTextChanged(String before, String after);
    }
}