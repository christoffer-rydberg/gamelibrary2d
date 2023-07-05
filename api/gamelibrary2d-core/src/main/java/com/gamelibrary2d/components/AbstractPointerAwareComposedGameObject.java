package com.gamelibrary2d.components;

import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;

public abstract class AbstractPointerAwareComposedGameObject<T extends GameObject>
        extends AbstractComposedGameObject<T> implements PointerDownAware, PointerMoveAware, PointerUpAware {

    @Override
    public boolean pointerDown(int id, int button, float x, float y, float transformedX, float transformedY) {
        T composition = getComposition();
        if (composition instanceof PointerDownAware) {
            return ((PointerDownAware) composition).pointerDown(id, button, x, y, transformedX, transformedY);
        }
        return false;
    }

    @Override
    public boolean pointerMove(int id, float x, float y, float transformedX, float transformedY) {
        T composition = getComposition();
        if (composition instanceof PointerMoveAware) {
            return ((PointerMoveAware) composition).pointerMove(id, x, y, transformedX, transformedY);
        }
        return false;
    }

    @Override
    public void swallowedPointerMove(int id) {
        T composition = getComposition();
        if (composition instanceof PointerMoveAware) {
            ((PointerMoveAware) composition).swallowedPointerMove(id);
        }
    }

    @Override
    public void pointerUp(int id, int button, float x, float y, float transformedX, float transformedY) {
        T composition = getComposition();
        if (composition instanceof PointerUpAware) {
            ((PointerUpAware) composition).pointerUp(id, button, x, y, transformedX, transformedY);
        }
    }
}
