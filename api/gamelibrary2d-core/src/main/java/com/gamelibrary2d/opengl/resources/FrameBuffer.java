package com.gamelibrary2d.opengl.resources;

import com.gamelibrary2d.disposal.Disposable;

public interface FrameBuffer extends Disposable {
    int bind();

    void unbind();

    void clear();

    int getPixel(int x, int y);
}