package com.gamelibrary2d.components.containers;

import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.denotations.*;
import com.gamelibrary2d.framework.Renderable;

import java.util.ArrayDeque;
import java.util.Deque;

public class NavigationPanel extends AbstractGameObject
        implements Clearable, PointerDownAware, PointerMoveAware, PointerUpAware {
    private final Point transformationPoint = new Point();
    private final Deque<GameObject> previous = new ArrayDeque<>();
    private GameObject current;
    private Rectangle bounds;

    public void navigateTo(GameObject obj, boolean rememberPrevious) {
        GameObject previous = this.current;
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
        GameObject previous = null;

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

    private void navigatedFrom(GameObject obj) {
        NavigationAware navigationAware = asNavigationAware(obj);
        if (navigationAware != null) {
            navigationAware.navigatedFrom(this);
        }
    }

    private void navigatedTo(GameObject obj) {
        NavigationAware navigationAware = asNavigationAware(obj);
        if (navigationAware != null) {
            navigationAware.navigatedTo(this);
        }
    }

    private NavigationAware asNavigationAware(GameObject obj) {
        return obj instanceof NavigationAware ? (NavigationAware) obj : null;
    }

    @Override
    public Renderable getRenderer() {
        return current.getRenderer();
    }

    @Override
    public Rectangle getBounds() {
        return bounds != null ? bounds : current.getBounds();
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }
}