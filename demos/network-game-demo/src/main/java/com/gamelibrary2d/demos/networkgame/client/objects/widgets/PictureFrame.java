package com.gamelibrary2d.demos.networkgame.client.objects.widgets;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.Quad;

import java.util.ArrayList;

public class PictureFrame implements Renderable {
    private final ArrayList<Renderable> sides;

    private PictureFrame(ArrayList<Renderable> sides) {
        this.sides = sides;
    }

    public static PictureFrame create(Rectangle outer, Rectangle inner, Color color, Disposer disposer) {
        ArrayList<Renderable> sides = new ArrayList<>(4);

        if (inner.getLowerX() > outer.getLowerX()) {
            Rectangle bounds = new Rectangle(outer.getLowerX(), outer.getLowerY(), inner.getLowerX(), outer.getUpperY());
            sides.add(createSideRenderer(color, bounds, disposer));
        }

        if (outer.getUpperX() > inner.getUpperX()) {
            Rectangle bounds = new Rectangle(inner.getUpperX(), outer.getLowerY(), outer.getUpperX(), outer.getUpperY());
            sides.add(createSideRenderer(color, bounds, disposer));
        }

        if (inner.getLowerY() > outer.getLowerY()) {
            Rectangle bounds = new Rectangle(outer.getLowerX(), outer.getLowerY(), outer.getUpperX(), inner.getLowerY());
            sides.add(createSideRenderer(color, bounds, disposer));
        }

        if (outer.getUpperY() > inner.getUpperY()) {
            Rectangle bounds = new Rectangle(outer.getLowerX(), inner.getUpperY(), outer.getUpperX(), outer.getUpperY());
            sides.add(createSideRenderer(color, bounds, disposer));
        }

        return new PictureFrame(sides);
    }

    private static Renderable createSideRenderer(Color color, Rectangle bounds, Disposer disposer) {
        ContentRenderer renderer = new SurfaceRenderer<>(Quad.create(bounds, disposer));
        renderer.setColor(color);
        return renderer;
    }

    @Override
    public void render(float alpha) {
        for (int i = 0; i < sides.size(); ++i) {
            sides.get(i).render(alpha);
        }
    }
}
