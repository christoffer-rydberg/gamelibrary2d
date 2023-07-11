package com.gamelibrary2d.demos.draganddrop;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.PointerState;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.denotations.PixelAware;
import com.gamelibrary2d.components.denotations.PointerAware;
import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.opengl.renderers.RenderCache;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.*;

public class DemoGameObject extends AbstractGameObject implements PointerAware, PixelAware, Updatable {
    private final RenderCache<SurfaceRenderer<Quad>> renderCache;
    private final DragAndDropBehavior dragAndDrop;

    public DemoGameObject(Rectangle bounds, Disposer disposer) {
        dragAndDrop = new DragAndDropBehavior(this);

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
        if (dragAndDrop.isDragged()) {
            renderCache.getRenderer().setColor(Color.BLUE);
        } else if (dragAndDrop.isHovered()) {
            renderCache.getRenderer().setColor(Color.GREEN);
        } else {
            renderCache.getRenderer().setColor(Color.WHITE);
        }
    }

    @Override
    public boolean pointerDown(PointerState pointerState, int id, int button, float transformedX, float transformedY) {
        return dragAndDrop.pointerDown(pointerState, id, button, transformedX, transformedY);
    }

    @Override
    public boolean pointerMove(PointerState pointerState, int id, float transformedX, float transformedY) {
        return dragAndDrop.pointerMove(pointerState, id, transformedX, transformedY);
    }

    @Override
    public void swallowedPointerMove(PointerState pointerState, int id) {
        dragAndDrop.swallowedPointerMove(pointerState, id);

    }

    @Override
    public void pointerUp(PointerState pointerState, int id, int button, float transformedX, float transformedY) {
        dragAndDrop.pointerUp(pointerState, id, button, transformedX, transformedY);
    }
}
