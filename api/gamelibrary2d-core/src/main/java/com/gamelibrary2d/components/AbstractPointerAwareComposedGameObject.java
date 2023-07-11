package com.gamelibrary2d.components;

import com.gamelibrary2d.PointerState;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;

public abstract class AbstractPointerAwareComposedGameObject<T extends GameObject>
        extends AbstractComposedGameObject<T> implements PointerDownAware, PointerMoveAware, PointerUpAware {

    @Override
    public boolean pointerDown(PointerState pointerState, int id, int button, float transformedX, float transformedY) {
        T composition = getComposition();
        if (composition instanceof PointerDownAware) {
            return ((PointerDownAware) composition).pointerDown(pointerState, id, button, transformedX, transformedY);
        }
        return false;
    }

    @Override
    public boolean pointerMove(PointerState pointerState, int id, float transformedX, float transformedY) {
        T composition = getComposition();
        if (composition instanceof PointerMoveAware) {
            return ((PointerMoveAware) composition).pointerMove(pointerState, id, transformedX, transformedY);
        }
        return false;
    }

    @Override
    public void swallowedPointerMove(PointerState pointerState, int id) {
        T composition = getComposition();
        if (composition instanceof PointerMoveAware) {
            ((PointerMoveAware) composition).swallowedPointerMove(pointerState, id);
        }
    }

    @Override
    public void pointerUp(PointerState pointerState, int id, int button, float transformedX, float transformedY) {
        T composition = getComposition();
        if (composition instanceof PointerUpAware) {
            ((PointerUpAware) composition).pointerUp(pointerState, id, button, transformedX, transformedY);
        }
    }
}
