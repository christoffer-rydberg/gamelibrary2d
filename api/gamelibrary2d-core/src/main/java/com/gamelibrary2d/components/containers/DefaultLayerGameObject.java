package com.gamelibrary2d.components.containers;

import com.gamelibrary2d.framework.Renderable;

public final class DefaultLayerGameObject<T extends Renderable> extends AbstractLayerGameObject<T> {
    private final Layer<T> layer;

    public DefaultLayerGameObject() {
        layer = new DefaultLayer<>();
    }

    public DefaultLayerGameObject(Layer<T> layer) {
        this.layer = layer;
    }

    @Override
    public Layer<T> getLayer() {
        return layer;
    }
}
