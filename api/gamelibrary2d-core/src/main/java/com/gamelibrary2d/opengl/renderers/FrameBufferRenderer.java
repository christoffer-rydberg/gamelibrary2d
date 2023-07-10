package com.gamelibrary2d.opengl.renderers;

import com.gamelibrary2d.OpenGL;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.opengl.ModelMatrix;
import com.gamelibrary2d.opengl.OpenGLState;
import com.gamelibrary2d.opengl.resources.FrameBuffer;

import static com.gamelibrary2d.OpenGL.*;

public class FrameBufferRenderer {
    private final FrameBuffer frameBuffer;

    public FrameBufferRenderer(FrameBuffer frameBuffer) {
        this.frameBuffer = frameBuffer;
    }

    public FrameBuffer getFrameBuffer() {
        return frameBuffer;
    }

    public void render(Renderable r, boolean clear, float offsetX, float offsetY, float alpha) {
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
            ModelMatrix.instance().translatef(offsetX, offsetY, 0);
            r.render(alpha);
            ModelMatrix.instance().popMatrix();
        } finally {
            OpenGLState.bindFrameBuffer(previousFbo);
        }
    }
}
