package com.gamelibrary2d.components;

import com.gamelibrary2d.KeyAndPointerState;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;

public abstract class AbstractPointerAwareComposedGameObject<T extends GameObject>
        extends AbstractComposedGameObject<T> implements PointerDownAware, PointerMoveAware, PointerUpAware {

    @Override
    public boolean pointerDown(KeyAndPointerState keyAndPointerState, int id, int button, float transformedX, float transformedY) {
        T composition = getComposition();
        if (composition instanceof PointerDownAware) {
            return ((PointerDownAware) composition).pointerDown(keyAndPointerState, id, button, transformedX, transformedY);
        }
        return false;
    }

    @Override
    public boolean pointerMove(KeyAndPointerState keyAndPointerState, int id, float transformedX, float transformedY) {
        T composition = getComposition();
        if (composition instanceof PointerMoveAware) {
            return ((PointerMoveAware) composition).pointerMove(keyAndPointerState, id, transformedX, transformedY);
        }
        return false;
    }

    @Override
    public void swallowedPointerMove(KeyAndPointerState keyAndPointerState, int id) {
        T composition = getComposition();
        if (composition instanceof PointerMoveAware) {
            ((PointerMoveAware) composition).swallowedPointerMove(keyAndPointerState, id);
        }
    }

    @Override
    public void pointerUp(KeyAndPointerState keyAndPointerState, int id, int button, float transformedX, float transformedY) {
        T composition = getComposition();
        if (composition instanceof PointerUpAware) {
            ((PointerUpAware) composition).pointerUp(keyAndPointerState, id, button, transformedX, transformedY);
        }
    }
}
