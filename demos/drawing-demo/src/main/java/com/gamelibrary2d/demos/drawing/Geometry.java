package com.gamelibrary2d.demos.drawing;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.opengl.buffers.PositionBuffer;
import com.gamelibrary2d.opengl.renderers.ArrayRenderer;

public class Geometry extends AbstractGameObject {
    private final PositionBuffer nodes;
    private final ArrayRenderer<PositionBuffer> arrayRenderer;
    private final Renderable renderer;

    public Geometry(PositionBuffer nodes, ArrayRenderer<PositionBuffer> arrayRenderer) {
        this.nodes = nodes;
        this.arrayRenderer = arrayRenderer;
        this.renderer = alpha -> {
            arrayRenderer.render(alpha, nodes, 0, nodes.size());
        };
    }

    public PositionBuffer nodes() {
        return nodes;
    }

    @Override
    public Renderable getRenderer() {
        return renderer;
    }

    public ArrayRenderer<PositionBuffer> getArrayRenderer() {
        return arrayRenderer;
    }

    @Override
    public Rectangle getBounds() {
        return Rectangle.EMPTY;
    }
}
