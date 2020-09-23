package com.gamelibrary2d.tools.particlegenerator.widgets;

import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.disposal.Disposer;
import com.gamelibrary2d.glUtil.PositionBuffer;
import com.gamelibrary2d.markers.Bounded;
import com.gamelibrary2d.renderers.LineRenderer;

public class Box implements Bounded {
    private final Rectangle bounds;
    private final PositionBuffer nodes;

    private Box(Rectangle bounds, PositionBuffer nodes) {
        this.bounds = bounds;
        this.nodes = nodes;
    }

    public static Box create(Rectangle bounds, Disposer disposer) {
        var nodes = PositionBuffer.create(disposer);
        nodes.add(bounds.xMin(), bounds.yMin());
        nodes.add(bounds.xMin(), bounds.yMax());
        nodes.add(bounds.xMax(), bounds.yMax());
        nodes.add(bounds.xMax(), bounds.yMin());
        nodes.add(bounds.xMin(), bounds.yMin());
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
