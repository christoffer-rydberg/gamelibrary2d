package com.gamelibrary2d.opengl.resources;

import com.gamelibrary2d.common.disposal.Disposable;
import com.gamelibrary2d.opengl.OpenGLState;

public interface FrameBuffer extends Disposable {
    Texture getTexture();

    int getId();

    int readPixel(int x, int y);

    boolean isVisible(int x, int y);

    void clear();

    default int bind() {
        return OpenGLState.bindFrameBuffer(getId());
    }

    default void unbind() {
        OpenGLState.unbindFrameBuffer(getId());
    }
}