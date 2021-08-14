package com.gamelibrary2d.components.objects;

import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;

public class AbstractPointerAwareComposedGameObject<T extends GameObject>
        extends AbstractComposedGameObject<T> implements PointerDownAware, PointerMoveAware, PointerUpAware {

    protected AbstractPointerAwareComposedGameObject() {

    }

    protected AbstractPointerAwareComposedGameObject(T content) {
        super(content);
    }

    @Override
    public boolean pointerDown(int id, int button, float x, float y, float projectedX, float projectedY) {
        T content = getContent();
        if (content instanceof PointerDownAware) {
            return ((PointerDownAware) content).pointerDown(id, button, x, y, projectedX, projectedY);
        }
        return false;
    }

    @Override
    public boolean pointerMove(int id, float x, float y, float projectedX, float projectedY) {
        T content = getContent();
        if (content instanceof PointerMoveAware) {
            return ((PointerMoveAware) content).pointerMove(id, x, y, projectedX, projectedY);
        }
        return false;
    }

    @Override
    public void pointerUp(int id, int button, float x, float y, float projectedX, float projectedY) {
        T content = getContent();
        if (content instanceof PointerUpAware) {
            ((PointerUpAware) content).pointerUp(id, button, x, y, projectedX, projectedY);
        }
    }
}