package com.gamelibrary2d.demos.draganddrop;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.denotations.PixelAware;
import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.opengl.renderers.RenderCache;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.*;

public class DemoGameObject extends AbstractGameObject implements Draggable, Hoverable, PixelAware, Updatable {
    private final RenderCache<SurfaceRenderer<Quad>> renderCache;
    private int pointersHovering = 0;
    private int pointersDragging = 0;

    public DemoGameObject(Rectangle bounds, Disposer disposer) {
        renderCache = RenderCache.create(
                new SurfaceRenderer<>(Quad.create(bounds, QuadShape.RADIAL_GRADIENT, disposer)),
                bounds,
                disposer);
    }

    @Override
    public Rectangle getBounds() {
        return renderCache.getBounds();
    }

    @Override
    protected void onRender(float alpha) {
        renderCache.render(alpha);
    }

    @Override
    public boolean isPixelVisible(float x, float y) {
        return renderCache.isPixelVisible(x, y, 40);
    }

    @Override
    public void update(float deltaTime) {
        updateColor();
    }

    private void updateColor() {
        if (isDragged()) {
            renderCache.getRenderer().setColor(Color.BLUE);
        } else if (isHovered()) {
            renderCache.getRenderer().setColor(Color.GREEN);
        } else {
            renderCache.getRenderer().setColor(Color.WHITE);
        }
    }

    private boolean isDragged() {
        return pointersDragging > 0;
    }

    private boolean isHovered() {
        return !isDragged() && pointersHovering > 0;
    }

    @Override
    public boolean onHoverStarted(int pointerId) {
        ++pointersHovering;
        return true;
    }

    @Override
    public boolean onHover() {
        return true;
    }

    @Override
    public void onHoverFinished(int pointerId) {
        pointersHovering = Math.max(0, pointersHovering - 1);
    }

    @Override
    public boolean onDragStarted(int pointerId, int button) {
        ++pointersDragging;
        return true;
    }

    @Override
    public boolean onDrag(float deltaX, float deltaY) {
        getPosition().add(deltaX, deltaY);
        return true;
    }

    @Override
    public void onDragFinished(int pointerId, int button) {
        pointersDragging = Math.max(0, pointersDragging - 1);
    }
}
