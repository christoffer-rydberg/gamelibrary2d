package com.gamelibrary2d.demos.draganddrop;

import com.gamelibrary2d.CoordinateSpace;
import com.gamelibrary2d.Point;
import com.gamelibrary2d.components.denotations.PixelAware;
import com.gamelibrary2d.denotations.Bounded;

public class HitDetection {
    private final Transformer transformer = new Transformer();

    public boolean isPixelVisible(Object obj, float x, float y) {
        if (obj instanceof PixelAware) {
            Point p = transformer.transform(obj, x, y);
            return ((PixelAware) obj).isPixelVisible(p.getX(), p.getY());
        } else if (obj instanceof Bounded) {
            Point p = transformer.transform(obj, x, y);
            return ((Bounded) obj).getBounds().contains(p.getX(), p.getY());
        } else {
            return false;
        }
    }

    private static class Transformer {
        private final Point point = new Point();

        private Point transform(Object obj, float x, float y) {
            point.set(x, y);
            if (obj instanceof CoordinateSpace) {
                point.transformTo((CoordinateSpace) obj);
            }

            return point;
        }
    }
}
