package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Bounded;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.opengl.buffers.PositionBuffer;
import com.gamelibrary2d.opengl.renderers.LineRenderer;

public class Box implements Bounded {
    private final Rectangle bounds;
    private final PositionBuffer nodes;

    private Box(Rectangle bounds, PositionBuffer nodes) {
        this.bounds = bounds;
        this.nodes = nodes;
    }

    public static Box create(Rectangle bounds, Disposer disposer) {
        PositionBuffer nodes = PositionBuffer.create(disposer);
        nodes.add(bounds.getLowerX(), bounds.getLowerY());
        nodes.add(bounds.getLowerX(), bounds.getUpperY());
        nodes.add(bounds.getUpperX(), bounds.getUpperY());
        nodes.add(bounds.getUpperX(), bounds.getLowerY());
        nodes.add(bounds.getLowerX(), bounds.getLowerY());
        return new Box(bounds, nodes);
    }

    public void render(LineRenderer renderer, float alpha) {
        renderer.render(alpha * 0.5f, nodes, 0, nodes.size());
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }
}
