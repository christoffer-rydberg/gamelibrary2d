package com.gamelibrary2d.components.containers;

import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.framework.Renderable;

public interface LayerObject<T extends Renderable> extends Layer<T>, GameObject {

}
