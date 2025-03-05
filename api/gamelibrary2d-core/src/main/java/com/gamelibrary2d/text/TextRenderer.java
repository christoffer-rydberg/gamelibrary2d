package com.gamelibrary2d.text;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.opengl.ModelMatrix;
import com.gamelibrary2d.opengl.OpenGLState;
import com.gamelibrary2d.opengl.renderers.BlendMode;
import com.gamelibrary2d.opengl.renderers.Renderer;
import com.gamelibrary2d.opengl.shaders.ShaderParameter;
import com.gamelibrary2d.opengl.shaders.ShaderProgram;

public class TextRenderer implements Renderer {
    private final ShaderProgram shaderProgram = OpenGLState.getPrimaryShaderProgram();
    private final float[] shaderParams = new float[ShaderParameter.MIN_PARAMETERS];

    private final Context renderContext = new Context();
    private final RenderRowAction renderRowAction = new RenderRowAction();

    private Font font;
    private BlendMode blendMode = BlendMode.TRANSPARENT;
    private HorizontalTextAlignment horizontalAlignment = HorizontalTextAlignment.LEFT;
    private VerticalTextAlignment verticalAlignment = VerticalTextAlignment.BASE_LINE;
    private float rowHeight;
    private float maxWidth = Float.MAX_VALUE;
    private float maxHeight = Float.MAX_VALUE;

    public TextRenderer() {
        this(null);
    }

    public TextRenderer(Font font)
    {
        this.font = font;
        this.shaderParams[ShaderParameter.TEXTURED] = 1;
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
    }

    public void setMaxWidth(float maxWidth) {
        this.maxWidth = maxWidth;
    }

    public float getMaxWidth() {
        return maxWidth;
    }

    public void setMaxHeight(float maxHeight) {
        this.maxHeight = maxHeight;
    }

    public float getMaxHeight() {
        return maxHeight;
    }

    public void setRowHeight(float rowHeight) {
        this.rowHeight = rowHeight;
    }

    public float getRowHeight() {
        return rowHeight;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    @Override
    public float getShaderParameter(int index) {
        return shaderParams[index];
    }

    @Override
    public void setShaderParameter(int index, float value) {
        shaderParams[index] = value;
    }

    public HorizontalTextAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalTextAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public VerticalTextAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalTextAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public void setAlignment(HorizontalTextAlignment horizontal, VerticalTextAlignment vertical) {
        setHorizontalAlignment(horizontal);
        setVerticalAlignment(vertical);
    }

    public Rectangle calculateBounds(String text, int offset, int len) {
        renderContext.prepare(this, text);
        ComputeBoundsRowAction rowAction = new ComputeBoundsRowAction();
        iterateRows(renderContext, font, offset, len, maxWidth, maxHeight, rowAction);
        return new Rectangle(rowAction.lowerX, rowAction.lowerY, rowAction.upperX, rowAction.upperY);
    }

    public int render(String text, float alpha, int offset, int len) {
        return render(text, alpha, offset, len, null);
    }

    public int render(String text, float alpha, int offset, int len, RowRenderer customRowRenderer) {
        ModelMatrix.instance().pushMatrix();

        OpenGLState.setBlendMode(blendMode);

        shaderParams[ShaderParameter.ALPHA] = alpha;

        shaderProgram.bind();
        shaderProgram.setParameters(shaderParams, 0, shaderParams.length);
        shaderProgram.applyParameters();

        this.renderRowAction.prepare(customRowRenderer);
        renderContext.prepare(this, text);
        int renderedCharacters = iterateRows(renderContext, font, offset, len, maxWidth, maxHeight, this.renderRowAction);

        ModelMatrix.instance().popMatrix();

        return renderedCharacters;
    }

    private static int iterateRows(Context ctx, Font font, int offset, int len, float maxWidth, float maxHeight, RowAction rowAction) {
        final String text = ctx.text;
        final int rowLimit = (int) (maxHeight / ctx.rowHeight);

        int currentRow = -1;
        int startOfRow = offset;
        int endOfText = len + offset;

        while (startOfRow < endOfText && currentRow < rowLimit) {
            ++currentRow;

            float textWidth = 0f;
            int endOfRow = startOfRow;
            while (true) {
                int endOfNewLine = consumeNewLine(text, endOfRow);
                if (endOfNewLine != endOfRow) {
                    rowAction.perform(ctx, currentRow, startOfRow, endOfRow - startOfRow, textWidth);
                    endOfRow = endOfNewLine;
                    break;
                }

                float charWidth = font.getTextWidth(text, endOfRow, 1);
                if (textWidth + charWidth > maxWidth) {
                    int whiteSpaceIndex = findLastWhiteSpaceIndex(text, startOfRow, endOfRow);
                    if (whiteSpaceIndex != -1) {
                        textWidth -= font.getTextWidth(text, whiteSpaceIndex, endOfRow - whiteSpaceIndex);
                        endOfRow = whiteSpaceIndex;
                    }

                    rowAction.perform(ctx, currentRow, startOfRow, endOfRow - startOfRow, textWidth);
                    break;
                }

                ++endOfRow;
                textWidth += charWidth;
                if (endOfRow == endOfText) {
                    rowAction.perform(ctx, currentRow, startOfRow, endOfRow - startOfRow, textWidth);
                    break;
                }
            }

            startOfRow = endOfRow < endOfText && text.charAt(endOfRow) == ' '
                    ? endOfRow + 1
                    : endOfRow;
        }

        return startOfRow - offset;
    }

    private static int consumeNewLine(String text, int index) {
        if (text.charAt(index) == '\n') {
            return index + 1;
        } else if (text.charAt(index) == '\r') {
            int next = index + 1;
            return next < text.length() && text.charAt(next) == '\n'
                    ? index + 2
                    : index + 1;
        } else {
            return index;
        }
    }

    private static int findLastWhiteSpaceIndex(String text, int start, int end) {
        for (int i = end - 1; i >= start; --i) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }

        return -1;
    }

    private static float offsetFromHorizontalAlignment(HorizontalTextAlignment alignment, float size) {
        switch (alignment) {
            case RIGHT:
                return -size;
            case CENTER:
                return -size / 2;
            default:
                return 0;
        }
    }

    private static float offsetFromVerticalAlignment(VerticalTextAlignment alignment, Font font) {
        switch (alignment) {
            case TOP:
                return -font.getAscent();
            case CENTER:
                return (font.getAscent() + font.getDescent()) / 2f - font.getAscent();
            case BOTTOM:
                return font.getDescent();
            case BASE_LINE:
            default:
                return 0;
        }
    }

    public interface RowRenderer {
        void render(Context context, int row, int offset, int len, float rowWidth);
    }

    private interface RowAction {
        void perform(Context context, int row, int offset, int len, float rowWidth);
    }

    public static class Context {
        private ShaderProgram shaderProgram;
        private Font font;
        private String text;
        private HorizontalTextAlignment horizontalAlignment;
        private VerticalTextAlignment verticalAlignment;
        private float rowHeight;

        public ShaderProgram getShaderProgram() {
            return shaderProgram;
        }

        public Font getFont() {
            return font;
        }

        public String getText() {
            return text;
        }

        public HorizontalTextAlignment getHorizontalAlignment() {
            return horizontalAlignment;
        }

        public VerticalTextAlignment getVerticalAlignment() {
            return verticalAlignment;
        }

        public float getRowHeight() {
            return rowHeight;
        }

        void prepare(TextRenderer textRenderer, String text) {
            shaderProgram = textRenderer.shaderProgram;
            font = textRenderer.font;
            this.text = text;
            horizontalAlignment = textRenderer.horizontalAlignment;
            verticalAlignment = textRenderer.verticalAlignment;
            rowHeight = textRenderer.rowHeight > 0 ? textRenderer.rowHeight : textRenderer.font.getHeight();
        }
    }

    private static class RenderRowAction implements RowAction {
        private RowRenderer customRowRenderer;

        public void prepare(RowRenderer customRowRenderer) {
            this.customRowRenderer = customRowRenderer;
        }

        @Override
        public void perform(Context context, int row, int offset, int len, float rowWidth) {
            float xOffset = offsetFromHorizontalAlignment(context.horizontalAlignment, rowWidth);
            float yOffset = offsetFromVerticalAlignment(context.verticalAlignment, context.font);

            if (xOffset != 0 || yOffset != 0) {
                ModelMatrix.instance().pushMatrix();
                ModelMatrix.instance().translatef(xOffset, yOffset, 0);
            }

            if (customRowRenderer != null) {
                customRowRenderer.render(context, row, offset, len, rowWidth);
            } else {
                context.font.render(context.shaderProgram, context.text, offset, len);
            }

            if (xOffset != 0 || yOffset != 0) {
                ModelMatrix.instance().popMatrix();
            }

            ModelMatrix.instance().translatef(0, -context.rowHeight, 0);
        }
    }

    private static class ComputeBoundsRowAction implements RowAction {
        private float lowerX;
        private float lowerY;
        private float upperX;
        private float upperY;

        @Override
        public void perform(Context context, int row, int textOffset, int textLength, float rowWidth) {
            float offsetX = offsetFromHorizontalAlignment(context.horizontalAlignment, rowWidth);
            float offsetY = offsetFromVerticalAlignment(context.verticalAlignment, context.font);

            float rowOffset = row * context.rowHeight;

            float lowerX = offsetX;
            float lowerY = offsetY - context.font.getDescent() - rowOffset;
            float upperX = rowWidth + offsetX;
            float upperY = offsetY + context.font.getAscent() - rowOffset;

            if (row == 0) {
                this.lowerX = lowerX;
                this.lowerY = lowerY;
                this.upperX = upperX;
                this.upperY = upperY;
            } else {
                this.lowerX = Math.min(this.lowerX, lowerX);
                this.lowerY = Math.min(this.lowerY, lowerY);
                this.upperX = Math.max(this.upperX, upperX);
                this.upperY = Math.max(this.upperY, upperY);
            }
        }
    }
}