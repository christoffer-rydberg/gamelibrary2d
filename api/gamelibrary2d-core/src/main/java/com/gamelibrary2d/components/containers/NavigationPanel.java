package com.gamelibrary2d.components.containers;

import com.gamelibrary2d.Point;
import com.gamelibrary2d.KeyAndPointerState;
import com.gamelibrary2d.Rectangle;
import com.gamelibrary2d.components.denotations.*;
import com.gamelibrary2d.denotations.Renderable;
import com.gamelibrary2d.components.AbstractGameObject;
import com.gamelibrary2d.denotations.Bounded;
import com.gamelibrary2d.denotations.Clearable;

import java.util.ArrayDeque;
import java.util.Deque;

public class NavigationPanel extends AbstractGameObject
        implements Clearable, PointerDownAware, PointerMoveAware, PointerUpAware {
    private final Point pointerPosition = new Point();
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
    public boolean pointerDown(KeyAndPointerState keyAndPointerState, int id, int button, float x, float y) {
        if (current instanceof PointerDownAware) {
            pointerPosition.set(x, y, this);
            return ((PointerDownAware) current).pointerDown(keyAndPointerState, id, button, pointerPosition.getX(), pointerPosition.getY());
        }

        return false;
    }

    @Override
    public boolean pointerMove(KeyAndPointerState keyAndPointerState, int id, float x, float y) {
        if (current instanceof PointerMoveAware) {
            pointerPosition.set(x, y, this);
            return ((PointerMoveAware) current).pointerMove(keyAndPointerState, id, pointerPosition.getX(), pointerPosition.getY());
        }

        return false;
    }

    @Override
    public void swallowedPointerMove(KeyAndPointerState keyAndPointerState, int id) {
        if (current instanceof PointerMoveAware) {
            ((PointerMoveAware) current).swallowedPointerMove(keyAndPointerState, id);
        }
    }

    @Override
    public void pointerUp(KeyAndPointerState keyAndPointerState, int id, int button, float x, float y) {
        if (current instanceof PointerUpAware) {
            pointerPosition.set(x, y, this);
            ((PointerUpAware) current).pointerUp(keyAndPointerState, id, button, pointerPosition.getX(), pointerPosition.getY());
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