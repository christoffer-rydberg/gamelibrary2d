package com.gamelibrary2d.components.containers;

import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.GameObject;

public interface LayerGameObject<T extends Renderable> extends Layer<T>, GameObject {

}
