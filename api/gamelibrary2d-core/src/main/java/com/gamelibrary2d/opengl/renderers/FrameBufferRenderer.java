package com.gamelibrary2d.opengl.renderers;

import com.gamelibrary2d.OpenGL;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.opengl.ModelMatrix;
import com.gamelibrary2d.opengl.OpenGLState;
import com.gamelibrary2d.opengl.resources.FrameBuffer;

import static com.gamelibrary2d.OpenGL.*;

public class FrameBufferRenderer {
    private final Rectangle area;
    private final FrameBuffer frameBuffer;

    public FrameBufferRenderer(Rectangle area, FrameBuffer frameBuffer) {
        this.area = area;
        this.frameBuffer = frameBuffer;
    }

    public FrameBuffer getFrameBuffer() {
        return frameBuffer;
    }

    public Rectangle getArea() {
        return area;
    }

    public void render(Renderable content, float alpha) {
        render(content, alpha, true);
    }

    public void render(Renderable content, float alpha, boolean clear) {
        int previousFbo = frameBuffer.bind();
        try {
            if (clear) {
                frameBuffer.clear();
            }

            // Fix for incorrect alpha blending. See:
            // https://community.khronos.org/t/alpha-blending-issues-when-drawing-frame-buffer-into-default-buffer/73958/3
            OpenGL.instance().glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

            ModelMatrix.instance().pushMatrix();
            ModelMatrix.instance().clearMatrix();
            ModelMatrix.instance().translatef(-area.getLowerX(), -area.getLowerY(), 0);
            content.render(alpha);
            ModelMatrix.instance().popMatrix();
        } finally {
            OpenGLState.bindFrameBuffer(previousFbo);
        }
    }

    public boolean isVisible(float x, float y, int threshold) {
        int previousFbo = frameBuffer.bind();
        try {
            return frameBuffer.isVisible(
                    (int) (x - area.getLowerX()),
                    (int) (y - area.getLowerY()),
                    threshold);
        } finally {
            OpenGLState.bindFrameBuffer(previousFbo);
        }
    }
}
