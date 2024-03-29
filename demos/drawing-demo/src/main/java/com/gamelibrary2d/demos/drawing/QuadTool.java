package com.gamelibrary2d.demos.drawing;

import com.gamelibrary2d.KeyAndPointerState;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;
import com.gamelibrary2d.event.DefaultEventPublisher;
import com.gamelibrary2d.event.EventListener;
import com.gamelibrary2d.event.EventPublisher;
import com.gamelibrary2d.functional.Func;
import com.gamelibrary2d.opengl.renderers.SurfaceRenderer;
import com.gamelibrary2d.opengl.resources.MutableQuad;
import com.gamelibrary2d.opengl.resources.Quad;

public class QuadTool implements Renderable, PointerDownAware, PointerMoveAware, PointerUpAware {
    private final EventPublisher<Quad> onCreated = new DefaultEventPublisher<>();
    private final Func<Rectangle, Quad> quadFactory;
    private final SurfaceRenderer<MutableQuad> inProgressRenderer;
    private final int drawButton;

    private QuadInProgress inProgress;

    public QuadTool(int drawButton, MutableQuad quadInProgress, Func<Rectangle, Quad> quadFactory) {
        this.drawButton = drawButton;
        this.inProgressRenderer = new SurfaceRenderer<>(quadInProgress);
        this.quadFactory = quadFactory;
    }

    public void addQuadCreatedListener(EventListener<Quad> listener) {
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
            inProgress = new QuadInProgress(x, y);
            return true;
        }

        return false;
    }

    @Override
    public boolean pointerMove(KeyAndPointerState state, int id, float x, float y) {
        if (isDrawing()) {
            inProgress.update(x, y);
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
            Quad quad = quadFactory.invoke(inProgress.getBounds());
            onCreated.publish(quad);
            inProgress = null;
        }
    }

    private class QuadInProgress implements Renderable {
        private final float x0;
        private final float y0;
        private float x1;
        private float y1;

        public QuadInProgress(float x0, float y0) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x0;
            this.y1 = y0;
        }

        public void update(float x1, float y1) {
            this.x1 = x1;
            this.y1 = y1;
        }

        float getLowerX() {
            return Math.min(x0, x1);
        }

        float getLowerY() {
            return Math.min(y0, y1);
        }

        float getUpperX() {
            return Math.max(x0, x1);
        }

        float getUpperY() {
            return Math.max(y0, y1);
        }

        public Rectangle getBounds() {
            return new Rectangle(
                    getLowerX(),
                    getLowerY(),
                    getUpperX(),
                    getUpperY()
            );
        }

        public void render(float alpha) {
            inProgressRenderer.getSurface().setBounds(
                    getLowerX(), getLowerY(), getUpperX(), getUpperY());

            inProgressRenderer.render(alpha);
        }
    }
}
