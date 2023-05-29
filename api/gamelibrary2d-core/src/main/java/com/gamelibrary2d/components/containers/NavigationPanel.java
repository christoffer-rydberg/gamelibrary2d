package com.gamelibrary2d.components.containers;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.denotations.NavigationAware;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;
import com.gamelibrary2d.denotations.Bounded;
import com.gamelibrary2d.denotations.Clearable;

import java.util.ArrayDeque;
import java.util.Deque;

public class NavigationPanel extends AbstractGameObject
        implements Clearable, PointerDownAware, PointerMoveAware, PointerUpAware {
    private final Point transformationPoint = new Point();
    private final Deque<Renderable> previous = new ArrayDeque<>();
    private Renderable current;
    private Rectangle bounds;

    public void navigateTo(Renderable obj, boolean rememberPrevious) {
        Renderable previous = this.current;
        if (previous != obj) {
            this.current = obj;
            if (rememberPrevious && previous != null) {
                this.previous.addLast(previous);
            }
            navigatedFrom(previous);
            navigatedTo(obj);
        }
    }

    public void goBack() {
        goBack(1);
    }

    public void goBack(int steps) {
        Renderable previous = null;

        while (steps > 0 && !this.previous.isEmpty()) {
            --steps;
            previous = this.previous.pollLast();
        }

        if (previous != null)
            navigateTo(previous, false);
    }

    @Override
    public void clear() {
        navigateTo(null, false);
        previous.clear();
    }

    @Override
    public boolean isAutoClearing() {
        return true;
    }

    @Override
    public boolean pointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {
        if (current instanceof PointerDownAware) {
            transformationPoint.set(transformedX, transformedY);
            transformationPoint.transformTo(this);
            return ((PointerDownAware) current).pointerDown(id, button, x, y, transformationPoint.getX(), transformationPoint.getY());
        }

        return false;
    }

    @Override
    public boolean pointerMove(int id, float x, float y, float transformedX, float transformedY) {
        if (current instanceof PointerMoveAware) {
            transformationPoint.set(transformedX, transformedY);
            transformationPoint.transformTo(this);
            return ((PointerMoveAware) current).pointerMove(id, x, y, transformationPoint.getX(), transformationPoint.getY());
        }

        return false;
    }

    @Override
    public void pointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {
        if (current instanceof PointerUpAware) {
            transformationPoint.set(transformedX, transformedY);
            transformationPoint.transformTo(this);
            ((PointerUpAware) current).pointerUp(id, button, x, y, transformationPoint.getX(), transformationPoint.getY());
        }
    }

    private void navigatedFrom(Renderable obj) {
        NavigationAware navigationAware = asNavigationAware(obj);
        if (navigationAware != null) {
            navigationAware.navigatedFrom(this);
        }
    }

    private void navigatedTo(Renderable obj) {
        NavigationAware navigationAware = asNavigationAware(obj);
        if (navigationAware != null) {
            navigationAware.navigatedTo(this);
        }
    }

    private NavigationAware asNavigationAware(Renderable obj) {
        return obj instanceof NavigationAware ? (NavigationAware) obj : null;
    }

    @Override
    public Rectangle getBounds() {
        return bounds != null ? bounds : getCurrentBounds();
    }

    private Rectangle getCurrentBounds() {
        return current instanceof Bounded
                ? ((Bounded) current).getBounds()
                : Rectangle.EMPTY;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    @Override
    protected void onRender(float alpha) {
        if (current != null) {
            current.render(alpha);
        }
    }
}