package com.gamelibrary2d.demos.splitscreen;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;
import com.gamelibrary2d.random.RandomInstance;

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
    protected void onRender(float alpha) {
        renderer.render(alpha);
    }
}
