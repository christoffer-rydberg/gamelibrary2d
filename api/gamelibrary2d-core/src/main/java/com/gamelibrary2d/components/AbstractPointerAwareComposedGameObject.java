package com.gamelibrary2d.components;

import com.gamelibrary2d.InputState;
import com.gamelibrary2d.components.denotations.PointerDownAware;
import com.gamelibrary2d.components.denotations.PointerMoveAware;
import com.gamelibrary2d.components.denotations.PointerUpAware;

public abstract class AbstractPointerAwareComposedGameObject<T extends GameObject>
        extends AbstractComposedGameObject<T> implements PointerDownAware, PointerMoveAware, PointerUpAware {

    @Override
    public boolean pointerDown(InputState inputState, int id, int button, float transformedX, float transformedY) {
        T composition = getComposition();
        if (composition instanceof PointerDownAware) {
            return ((PointerDownAware) composition).pointerDown(inputState, id, button, transformedX, transformedY);
        }
        return false;
    }

    @Override
    public boolean pointerMove(InputState inputState, int id, float transformedX, float transformedY) {
        T composition = getComposition();
        if (composition instanceof PointerMoveAware) {
            return ((PointerMoveAware) composition).pointerMove(inputState, id, transformedX, transformedY);
        }
        return false;
    }

    @Override
    public void swallowedPointerMove(InputState inputState, int id) {
        T composition = getComposition();
        if (composition instanceof PointerMoveAware) {
            ((PointerMoveAware) composition).swallowedPointerMove(inputState, id);
        }
    }

    @Override
    public void pointerUp(InputState inputState, int id, int button, float transformedX, float transformedY) {
        T composition = getComposition();
        if (composition instanceof PointerUpAware) {
            ((PointerUpAware) composition).pointerUp(inputState, id, button, transformedX, transformedY);
        }
    }
}
