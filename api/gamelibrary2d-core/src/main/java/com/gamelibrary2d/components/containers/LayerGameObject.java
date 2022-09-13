package com.gamelibrary2d.components.containers;

import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.framework.Renderable;

public interface LayerGameObject<T extends Renderable> extends Layer<T>, GameObject {

}
