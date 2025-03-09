package com.gamelibrary2d.demos.draganddrop;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.KeyAndPointerState;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.denotations.PointerAware;
import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.disposal.Disposer;
import com.gamelibrary2d.opengl.renderers.Canvas;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.*;

public class DemoGameObject extends AbstractGameObject implements PointerAware, PixelAware, Updatable {
    private final Canvas<SurfaceRenderer<Quad>> canvas;
    private final DragAndDropBehavior dragAndDropBehavior;
    private Interaction interaction = Interaction.None;

    public DemoGameObject(Rectangle bounds, Disposer disposer) {
        dragAndDropBehavior = new DragAndDropBehavior(this);

        canvas = Canvas.create(
                new SurfaceRenderer<>(Quad.create(bounds, QuadShape.RADIAL_GRADIENT, disposer)),
                bounds,
                false,
                disposer);
    }

    @Override
    public Rectangle getBounds() {
        return canvas.getBounds();
    }

    @Override
    public boolean isPixelVisible(float x, float y) {
        return canvas.isPixelVisible(x, y, 40);
    }

    @Override
    protected void onRender(float alpha) {
        canvas.render(alpha);
    }

    @Override
    public void update(float deltaTime) {
        Interaction interaction = getInteraction();
        if (this.interaction != interaction) {
            this.interaction = interaction;
            refreshCanvas();
        }
    }

    private void refreshCanvas() {
        canvas.refresh();
        switch (interaction) {
            case None:
                canvas.getContent().setColor(Color.WHITE);
                break;
            case Hovering:
                canvas.getContent().setColor(Color.GREEN);
                break;
            case Dragging:
                canvas.getContent().setColor(Color.BLUE);
                break;
        }
    }

    private Interaction getInteraction() {
        if (dragAndDropBehavior.isDragged()) {
            return Interaction.Dragging;
        } else if (dragAndDropBehavior.isHovered()) {
            return Interaction.Hovering;
        } else {
            return Interaction.None;
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

    private enum Interaction {
        None,
        Hovering,
        Dragging
    }
}
