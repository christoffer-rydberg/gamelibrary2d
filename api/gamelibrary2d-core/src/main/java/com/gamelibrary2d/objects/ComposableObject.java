package com.gamelibrary2d.objects;

import com.gamelibrary2d.framework.Renderable;

public interface ComposableObject<T extends Renderable> extends GameObject {

    T getContent();
}
