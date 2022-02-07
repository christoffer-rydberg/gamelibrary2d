package com.gamelibrary2d.renderers;

import com.gamelibrary2d.framework.OpenGL;
import com.gamelibrary2d.glUtil.PositionBuffer;
import com.gamelibrary2d.glUtil.ShaderProgram;

public class LineRenderer extends AbstractArrayRenderer<PositionBuffer> {
    public static final short SOLID = (byte) 0xFFFF;
    public static final short DOTTED = 0b0101010101010101;
    public static final short DOTTED_SPARSE = 0b0001000100010001;
    public static final short DOTTED_SPARSE2 = 0b0000000100000001;
    public static final short DASHED = 0b0111011101110111;
    public static final short DASHED_SPARSE = 0b0000011100000111;
    public static final short DASHED_SPARSE2 = 0b0000001100000011;
    public static final short DASHED_LONG = 0b0001111100011111;
    public static final short DASHED_LONG2 = 0b0111111101111111;

    private short pattern = SOLID;
    private int patternFactor = 1;
    private boolean loop;
    private float lineWidth;

    public LineRenderer() {
        this(1f);
    }

    public LineRenderer(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public short getPattern() {
        return pattern;
    }

    public void setPattern(short pattern) {
        this.pattern = pattern;
    }

    public int getPatternFactor() {
        return patternFactor;
    }

    public void setPatternFactor(int patternFactor) {
        this.patternFactor = patternFactor;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    @Override
    protected int getOpenGlDrawMode() {
        return isLoop() ? OpenGL.GL_LINE_LOOP : OpenGL.GL_LINE_STRIP;
    }

    @Override
    protected void renderPrepare(ShaderProgram shaderProgram) {
        if (pattern == SOLID) {
            OpenGL.instance().glDisable(OpenGL.GL_LINE_STIPPLE);
        } else {
            OpenGL.instance().glEnable(OpenGL.GL_LINE_STIPPLE);
            OpenGL.instance().glLineStipple(patternFactor, pattern);
        }

        OpenGL.instance().glLineWidth(lineWidth);
    }
}
