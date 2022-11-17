package com.gamelibrary2d.demos.splitscreen;

import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.common.random.RandomInstance;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.common.denotations.Updatable;
import com.gamelibrary2d.framework.Renderable;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;

public class SpaceCraft extends AbstractGameObject implements Updatable {
    private final Rectangle area;
    private final ContentRenderer renderer;
    private final Point delta;

    public SpaceCraft(Rectangle area, ContentRenderer renderer) {
        this.area = area;
        this.renderer = renderer;
        float direction = RandomInstance.get().nextFloat() * 360f;
        delta = new Point(0, 100);
        delta.rotate(direction);
    }

    private static float getInRange(float value, float min, float max) {
        float width = max - min;
        float dist = (value - min) / width;
        float distDecimals = dist - (int) dist;
        if (distDecimals < 0) {
            distDecimals += 1f;
        }

        return distDecimals * width + min;
    }

    private static void wrap(Rectangle area, Point position) {
        float x = getInRange(position.getX(), area.getLowerX(), area.getUpperX());
        float y = getInRange(position.getY(), area.getLowerY(), area.getUpperY());
        position.set(x, y);
        position.add(area.getLowerX(), area.getLowerY());
    }

    @Override
    public void update(float deltaTime) {
        setRotation(delta.getAngleDegrees());
        getPosition().add(delta.getX() * deltaTime, delta.getY() * deltaTime);
        wrap(area, getPosition());
    }

    @Override
    public Rectangle getBounds() {
        return renderer.getBounds();
    }

    @Override
    public Renderable getRenderer() {
        return renderer;
    }
}
