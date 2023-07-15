package com.gamelibrary2d.demos.draganddrop;

import com.gamelibrary2d.InputState;
import com.gamelibrary2d.components.GameObject;
import com.gamelibrary2d.components.denotations.PointerAware;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DragAndDropBehavior implements PointerAware {
    private final Set<Integer> hoveringPointers = new HashSet<>();
    private final HitDetection hitDetection = new HitDetection();
    private final Map<Integer, DragInteraction> dragInteractions = new HashMap<>();
    private final GameObject obj;

    public DragAndDropBehavior(GameObject obj) {
        this.obj = obj;
    }

    public boolean isDragged() {
        return !dragInteractions.isEmpty();
    }

    public boolean isHovered() {
        return !hoveringPointers.isEmpty();
    }

    @Override
    public boolean pointerDown(InputState inputState, int id, int button, float x, float y) {
        if (hitDetection.isPixelVisible(obj, x, y)) {
            DragInteraction interaction = dragInteractions.get(id);
            if (interaction == null) {
                hoveringPointers.remove(id);
                dragInteractions.put(id, new DragInteraction(button, x, y));
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean pointerMove(InputState inputState, int id, float x, float y) {
        DragInteraction interaction = dragInteractions.get(id);
        if (interaction != null) {
            interaction.update(x, y);
            return true;
        }

        if (!inputState.isPointerDown(id) && hitDetection.isPixelVisible(obj, x, y)) {
            hoveringPointers.add(id);
            return true;
        } else {
            hoveringPointers.remove(id);
            return false;
        }
    }

    @Override
    public void swallowedPointerMove(InputState inputState, int id) {
        hoveringPointers.remove(id);
    }

    @Override
    public void pointerUp(InputState inputState, int id, int button, float x, float y) {
        DragInteraction interaction = dragInteractions.get(id);
        if (interaction != null && interaction.button == button) {
            dragInteractions.remove(id);
        }
    }

    private class DragInteraction {
        private final int button;
        private float prevPointerPosX;
        private float prevPointerPosY;

        public DragInteraction(int button, float pointerPosX, float pointerPosY) {
            this.button = button;
            this.prevPointerPosX = pointerPosX;
            this.prevPointerPosY = pointerPosY;
        }

        public void update(float pointerPosX, float pointerPosY) {
            obj.getPosition().add(pointerPosX - prevPointerPosX, pointerPosY - prevPointerPosY);
            prevPointerPosX = pointerPosX;
            prevPointerPosY = pointerPosY;
        }
    }
}
