package com.gamelibrary2d.demos.draganddrop;

import com.gamelibrary2d.Color;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.AbstractPointerAwareGameObject;
import com.gamelibrary2d.denotations.Updatable;
import com.gamelibrary2d.opengl.renderers.ContentRenderer;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.Surface;

public class DemoGameObject extends AbstractPointerAwareGameObject implements Updatable {
    private final ContentRenderer renderer;
    private int pointersAbove = 0;
    private int pointersDragging = 0;

    public DemoGameObject(Surface surface) {
        renderer = new SurfaceRenderer<>(surface);
    }

    @Override
    public Rectangle getBounds() {
        return renderer.getBounds();
    }

    @Override
    protected void onRender(float alpha) {
        renderer.render(alpha);
    }

    @Override
    public boolean isPixelVisible(float x, float y) {
        return getBounds().contains(x, y);
    }

    @Override
    protected boolean onPointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {
        return false;
    }

    @Override
    protected void onPointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {

    }

    @Override
    protected boolean isTrackingPointerPositions() {
        return true;
    }

    @Override
    protected void onPointerEntered(int id) {
        ++pointersAbove;
    }

    @Override
    protected void onPointerLeft(int id) {
        pointersAbove = Math.max(0, pointersAbove - 1);
    }

    @Override
    protected boolean onPointerMove(int id, float x, float y, float transformedX, float transformedY) {
        return true;
    }

    @Override
    public void clearPointerState() {
        super.clearPointerState();
        pointersAbove = 0;
        pointersDragging = 0;
    }

    public void onDragStarted() {
        ++pointersDragging;
    }

    public void onDragFinished() {
        pointersDragging = Math.max(0, pointersDragging - 1);
    }

    @Override
    public void update(float deltaTime) {
        updateColor();
    }

    private void updateColor() {
        if (isDragged()) {
            renderer.setColor(Color.BLUE);
        } else if (isHovered()) {
            renderer.setColor(Color.GREEN);
        } else {
            renderer.setColor(Color.WHITE);
        }
    }

    private boolean isDragged() {
        return pointersDragging > 0;
    }

    private boolean isHovered() {
        return !isDragged() && pointersAbove > 0;
    }
}
