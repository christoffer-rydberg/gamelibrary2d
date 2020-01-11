package com.gamelibrary2d.objects;

import com.gamelibrary2d.common.Color;
import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.glUtil.ModelMatrix;
import com.gamelibrary2d.renderers.TextRenderer;
import com.gamelibrary2d.rendering.HorizontalAlignment;
import com.gamelibrary2d.rendering.RenderSettings;
import com.gamelibrary2d.rendering.VerticalAlignment;
import com.gamelibrary2d.resources.Font;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TextObject extends InteractiveObject {

    private final List<TextChangedListener> textChangedListeners = new CopyOnWriteArrayList<>();
    private final Point textPositionOffset = new Point();
    private float rowWidth, rowHeight;
    private String text = "";
    private TextRenderer textRenderer;
    private TextRenderer focusedTextRenderer;

    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
    private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;

    private Color fontColor;
    private Color focusedFontColor;

    private String breakString;
    private int textOffset;
    private int rowCount = -1;
    private int charactersRendered;

    public void addTextChangedListener(TextChangedListener listener) {
        textChangedListeners.add(listener);
    }

    public void removeTextChangedListener(TextChangedListener listener) {
        textChangedListeners.remove(listener);
    }

    public Color getFontColor() {
        return fontColor;
    }

    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
    }

    public Color getFocusedFontColor() {
        return focusedFontColor;
    }

    public void setFocusedFontColor(Color focusedFontColor) {
        this.focusedFontColor = focusedFontColor;
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    public void setTextRenderer(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    public TextRenderer getFocusedTextRenderer() {
        return focusedTextRenderer;
    }

    public void setFocusedTextRenderer(TextRenderer focusedTextRenderer) {
        this.focusedTextRenderer = focusedTextRenderer;
    }

    public Point getTextPositionOffset() {
        return textPositionOffset;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public float getRowWidth() {
        return rowWidth;
    }

    public void setRowWidth(float rowWidth) {
        this.rowWidth = rowWidth;
    }

    public float getRowHeight() {
        return rowHeight;
    }

    public void setRowHeight(float rowMargin) {
        this.rowHeight = rowMargin;
    }

    public int getCharactersRenderers() {
        return charactersRendered;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getTextOffset() {
        return textOffset;
    }

    public void setTextOffset(int textOffset) {
        this.textOffset = textOffset;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        String oldValue = this.text;
        this.text = text;
        for (TextChangedListener listener : textChangedListeners) {
            listener.onTextChanged(this, oldValue, text);
        }
    }

    /**
     * Rendering will break on rows matching the specified break string. This is
     * useful when creating dialogs. Rendering the next part of the dialog is done
     * by setting the {@link #setTextOffset(int) text offset} to the first index
     * after the break string.
     *
     * @return The break string. Null means that this functionality is disabled.
     */
    public String getBreakString() {
        return breakString;
    }

    /**
     * Sets the specified {@link #getBreakString() break string}.
     *
     * @param breakString The break string, or null to disable this functionality.
     */
    public void setBreakString(String breakString) {
        this.breakString = breakString;
    }

    private boolean isBreakString(String text, int offset, int len) {
        if (breakString == null || len != breakString.length())
            return false;
        for (int i = 0; i < len; ++i) {
            if (text.charAt(offset + i) != breakString.charAt(i))
                return false;
        }
        return true;
    }

    @Override
    protected void onRender(float alpha) {
        super.onRender(alpha);
        if (isFocused() && focusedTextRenderer != null) {
            onRender(focusedTextRenderer, focusedFontColor, alpha);
        } else if (textRenderer != null) {
            onRender(textRenderer, fontColor, alpha);
        }
    }

    private void onRender(TextRenderer textRenderer, Color color, float alpha) {

        if (color != null) {
            textRenderer.updateSettings(RenderSettings.COLOR_R, color.getR(), color.getG(), color.getB(), color.getA());
        }

        float usedRowWidth = rowWidth > 0 ? rowWidth : Integer.MAX_VALUE;
        float usedRowHeight = rowHeight > 0 ? rowHeight : textRenderer.getFont().textHeight();
        renderRows(textRenderer, usedRowWidth, usedRowHeight, alpha, true);
    }

    public int determineRequiredRows(TextRenderer renderer) {
        return renderRows(renderer, getRowWidth(), getRowHeight(), 1f, false);
    }

    private int renderRows(TextRenderer textRenderer, float rowWidth, float rowHeight, float alpha, boolean render) {

        final Font font = textRenderer.getFont();

        final int rowCount = getRowCount() > 0 ? getRowCount() : Integer.MAX_VALUE;

        int currentRow = 0;

        int rowStart = getTextOffset();

        while (rowStart < text.length() && currentRow < rowCount) {

            // Get the end of the row
            int rowEnd = font.getRowEnd(text, rowStart, rowWidth, true);

            boolean endOfText = rowEnd == text.length();
            int newLineSize = endOfText ? 0 : Font.getNewLineSize(text, rowEnd);
            boolean newLineSeparatorFound = newLineSize > 0;
            if (!endOfText && !newLineSeparatorFound) {
                // Back up to avoid cutting words
                while (text.charAt(rowEnd) != ' ') {
                    --rowEnd;
                }
            }

            int len = rowEnd - rowStart;

            boolean renderBreak = isBreakString(text, rowStart, len);

            if (render && !renderBreak)
                renderRow(textRenderer, alpha, font.textWidth(text, rowStart, len), rowHeight, rowStart, len);

            rowStart = newLineSeparatorFound ? rowEnd + newLineSize : consumeSeparators(text, rowEnd);

            if (renderBreak) {
                if (currentRow != 0) {
                    ++currentRow;
                    break;
                }
            } else {
                ++currentRow;
            }
        }

        if (render)
            charactersRendered = rowStart;

        return currentRow;
    }

    private int consumeSeparators(String text, int index) {
        while (index < text.length() && text.charAt(index) == ' ')
            ++index;
        return index;
    }

    private void renderRow(TextRenderer textRenderer, float alpha, float rowWidth, float rowHeight, int offset,
                           int len) {

        final float xOffset = offsetFromHorizontalAlignment(rowWidth) + textPositionOffset.getX();
        final float yOffset = offsetFromVerticalAlignment(textRenderer.getFont().textHeight())
                + textPositionOffset.getY();

        if (xOffset != 0 || yOffset != 0) {
            ModelMatrix.instance().pushMatrix();
            ModelMatrix.instance().translatef(xOffset, yOffset, 0);
        }

        textRenderer.setText(text, offset, len);
        textRenderer.render(alpha);

        if (xOffset != 0 || yOffset != 0) {
            ModelMatrix.instance().popMatrix();
        }

        // New row
        ModelMatrix.instance().translatef(0, -rowHeight, 0);
    }

    private float offsetFromHorizontalAlignment(float size) {
        switch (horizontalAlignment) {
            case RIGHT:
                return -size;
            case CENTER:
                return -size / 2;
            default:
                return 0;
        }
    }

    private float offsetFromVerticalAlignment(float size) {
        switch (verticalAlignment) {
            case TOP:
                return -size;
            case CENTER:
                return -size / 2;
            default:
                return 0;
        }
    }
}