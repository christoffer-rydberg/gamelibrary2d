package com.gamelibrary2d.demos.drawing;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.KeyAndPointerState;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;
import com.gamelibrary2d.event.DefaultEventPublisher;
import com.gamelibrary2d.event.EventListener;
import com.gamelibrary2d.event.EventPublisher;
import com.gamelibrary2d.functional.Factory;

public class GeometryTool implements Renderable, PointerDownAware, PointerMoveAware, PointerUpAware {
    private final EventPublisher<Geometry> onCreated = new DefaultEventPublisher<>();
    private final Factory<Geometry> geometryFactory;
    private final int drawButton;
    private final Point prevNode;
    private final float minNodeInterval;

    private Geometry inProgress;

    public GeometryTool(int drawButton, Factory<Geometry> geometryFactory, float minNodeInterval) {
        this.drawButton = drawButton;
        this.geometryFactory = geometryFactory;
        this.minNodeInterval = minNodeInterval;
        this.prevNode = new Point();
    }

    public void addGeometryCreatedListener(EventListener<Geometry> listener) {
        onCreated.addListener(listener);
    }

    @Override
    public void render(float alpha) {
        if (isDrawing()) {
            inProgress.render(alpha);
        }
    }

    private boolean isDrawing() {
        return inProgress != null;
    }

    @Override
    public boolean pointerDown(KeyAndPointerState state, int id, int button, float x, float y) {
        if (drawButton == button) {
            inProgress = geometryFactory.create();
            inProgress.nodes().add(x, y);
            prevNode.set(x, y);
            return true;
        }

        return false;
    }

    @Override
    public boolean pointerMove(KeyAndPointerState state, int id, float x, float y) {
        if (isDrawing()) {
            if (prevNode.getDistance(x, y) > minNodeInterval) {
                inProgress.nodes().add(x, y);
                prevNode.set(x, y);
            }
            return true;
        }

        return false;
    }

    @Override
    public void swallowedPointerMove(KeyAndPointerState state, int id) {

    }

    @Override
    public void pointerUp(KeyAndPointerState state, int id, int button, float x, float y) {
        if (drawButton == button) {
            onCreated.publish(inProgress);
            inProgress = null;
        }
    }
}
