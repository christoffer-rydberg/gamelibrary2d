package com.gamelibrary2d.components.containers;

import com.gamelibrary2d.Projection;
import com.gamelibrary2d.common.Point;
import com.gamelibrary2d.common.Rectangle;
import com.gamelibrary2d.components.denotations.*;
import com.gamelibrary2d.components.objects.AbstractGameObject;
import com.gamelibrary2d.components.objects.GameObject;

import java.util.ArrayDeque;
import java.util.Deque;

public class NavigationPanel extends AbstractGameObject
        implements Clearable, PointerDownAware, PointerMoveAware, PointerUpAware {
    private final Point projectionOutput = new Point();
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
    public boolean pointerDown(int id, int button, float x, float y, float projectedX, float projectedY) {
        if (current instanceof PointerDownAware) {
            Projection.projectTo(this, projectedX, projectedY, projectionOutput);
            return ((PointerDownAware) current).pointerDown(id, button, x, y, projectionOutput.getX(), projectionOutput.getY());
        }

        return false;
    }

    @Override
    public boolean pointerMove(int id, float x, float y, float projectedX, float projectedY) {
        if (current instanceof PointerMoveAware) {
            Projection.projectTo(this, projectedX, projectedY, projectionOutput);
            return ((PointerMoveAware) current).pointerMove(id, x, y, projectionOutput.getX(), projectionOutput.getY());
        }

        return false;
    }

    @Override
    public void pointerUp(int id, int button, float x, float y, float projectedX, float projectedY) {
        if (current instanceof PointerUpAware) {
            Projection.projectTo(this, projectedX, projectedY, projectionOutput);
            ((PointerUpAware) current).pointerUp(id, button, x, y, projectionOutput.getX(), projectionOutput.getY());
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
    protected void onRender(float alpha) {
        if (current != null) {
            current.render(alpha);
        }
    }

    @Override
    public Rectangle getBounds() {
        return bounds != null ? bounds : current.getBounds();
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }
}