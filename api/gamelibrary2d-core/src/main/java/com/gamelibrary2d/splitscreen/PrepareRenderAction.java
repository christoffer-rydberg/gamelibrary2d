package com.gamelibrary2d.splitscreen;

import com.gamelibrary2d.Rectangle;

public interface PrepareRenderAction<T> {
    void invoke(T param, Rectangle viewArea);
}
