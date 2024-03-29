package com.gamelibrary2d.opengl.resources;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.OpenGL;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.opengl.OpenGLState;

import java.nio.ByteBuffer;

public class TextureFrameBuffer implements FrameBuffer {
    private final int id;
    private final Texture texture;
    private final ByteBuffer pixelReadBuffer = ByteBuffer.allocateDirect(4);
    private Color backgroundColor = Color.TRANSPARENT;

    private TextureFrameBuffer(int id, Texture texture) {
        this.id = id;
        this.texture = texture;
    }

    public static TextureFrameBuffer create(int width, int height, Disposer disposer) {
        Texture texture = DefaultTexture.create(width, height, disposer);
        return create(texture, disposer);
    }

    public static TextureFrameBuffer create(Texture texture, Disposer disposer) {
        OpenGL openGl = OpenGL.instance();

        // Create a frame buffer
        int fbo = openGl.glGenFramebuffers();
        openGl.glBindFramebuffer(OpenGL.GL_FRAMEBUFFER, fbo);

        // Attach a texture to the frame buffer
        openGl.glFramebufferTexture2D(OpenGL.GL_FRAMEBUFFER, OpenGL.GL_COLOR_ATTACHMENT0, OpenGL.GL_TEXTURE_2D, texture.getId(), 0);

        // Unbind
        openGl.glBindRenderbuffer(OpenGL.GL_RENDERBUFFER, 0);
        openGl.glBindFramebuffer(OpenGL.GL_FRAMEBUFFER, 0);

        TextureFrameBuffer frameBuffer = new TextureFrameBuffer(fbo, texture);

        disposer.registerDisposal(frameBuffer);

        return frameBuffer;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Texture getTexture() {
        return texture;
    }

    @Override
    public int getPixel(int x, int y) {
        pixelReadBuffer.clear();

        OpenGL.instance().glReadPixels(
                x,
                y,
                1,
                1,
                OpenGL.GL_RGBA,
                OpenGL.GL_UNSIGNED_BYTE,
                pixelReadBuffer);

        return pixelReadBuffer.getInt(0);
    }

    @Override
    public int bind() {
        return OpenGLState.bindFrameBuffer(id);
    }

    @Override
    public void unbind() {
        OpenGLState.unbindFrameBuffer(id);
    }

    @Override
    public void clear() {
        OpenGL.instance().glClearColor(backgroundColor.getR(), backgroundColor.getG(), backgroundColor.getB(), backgroundColor.getA());
        OpenGL.instance().glClear(OpenGL.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void dispose() {
        OpenGL.instance().glDeleteFramebuffers(id);
    }
}