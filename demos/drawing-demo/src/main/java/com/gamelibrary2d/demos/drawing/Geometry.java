package com.gamelibrary2d.demos.drawing;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.opengl.buffers.PositionBuffer;
import com.gamelibrary2d.opengl.renderers.ArrayRenderer;

public class Geometry extends AbstractGameObject {
    private final PositionBuffer nodes;
    private final ArrayRenderer<PositionBuffer> arrayRenderer;

    public Geometry(PositionBuffer nodes, ArrayRenderer<PositionBuffer> arrayRenderer) {
        this.nodes = nodes;
        this.arrayRenderer = arrayRenderer;
    }

    public PositionBuffer nodes() {
        return nodes;
    }

    public ArrayRenderer<PositionBuffer> getArrayRenderer() {
        return arrayRenderer;
    }

    @Override
    public Rectangle getBounds() {
        return Rectangle.EMPTY;
    }

    @Override
    protected void onRender(float alpha) {
        arrayRenderer.render(alpha, nodes, 0, nodes.size());
    }
}
