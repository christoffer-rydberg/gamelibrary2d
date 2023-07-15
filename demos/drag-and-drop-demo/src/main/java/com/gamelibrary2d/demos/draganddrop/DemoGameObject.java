package com.gamelibrary2d.demos.draganddrop;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.KeyAndPointerState;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.denotations.PointerAware;
import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.opengl.renderers.RenderCache;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.*;

public class DemoGameObject extends AbstractGameObject implements PointerAware, PixelAware, Updatable {
    private final RenderCache<SurfaceRenderer<Quad>> renderCache;
    private final DragAndDropBehavior dragAndDropBehavior;

    public DemoGameObject(Rectangle bounds, Disposer disposer) {
        dragAndDropBehavior = new DragAndDropBehavior(this);

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
    public boolean isPixelVisible(float x, float y) {
        return renderCache.isPixelVisible(x, y, 40);
    }

    @Override
    protected void onRender(float alpha) {
        renderCache.render(alpha);
    }

    @Override
    public void update(float deltaTime) {
        if (dragAndDropBehavior.isDragged()) {
            renderCache.getRenderer().setColor(Color.BLUE);
        } else if (dragAndDropBehavior.isHovered()) {
            renderCache.getRenderer().setColor(Color.GREEN);
        } else {
            renderCache.getRenderer().setColor(Color.WHITE);
        }
    }

    @Override
    public boolean pointerDown(KeyAndPointerState state, int id, int button, float x, float y) {
        return dragAndDropBehavior.pointerDown(state, id, button, x, y);
    }

    @Override
    public boolean pointerMove(KeyAndPointerState state, int id, float x, float y) {
        return dragAndDropBehavior.pointerMove(state, id, x, y);
    }

    @Override
    public void swallowedPointerMove(KeyAndPointerState state, int id) {
        dragAndDropBehavior.swallowedPointerMove(state, id);

    }

    @Override
    public void pointerUp(KeyAndPointerState state, int id, int button, float x, float y) {
        dragAndDropBehavior.pointerUp(state, id, button, x, y);
    }
}
