package com.gamelibrary2d.splitscreen;

import com.gamelibrary2d.Rectangle;

public interface PrepareUpdateAction<T> {
    void invoke(T param, Rectangle viewArea, float deltaTime);
}
