package com.gamelibrary2d;

import com.gamelibrary2d.components.denotations.*;
import com.gamelibrary2d.denotations.Container;

import java.util.ArrayList;
import java.util.List;

public class FocusManager {

    private static final List<Object> focused = new ArrayList<>();

    private static final List<Object> focusedByPointer = new ArrayList<>();

    private static final List<Object> iterationList = new ArrayList<>();

    private static boolean pointerActive;

    static void onPointerActive() {
        pointerActive = true;
    }

    static void onPointerInactive() {
        pointerActive = false;
        focused.addAll(focusedByPointer);
        focusedByPointer.clear();
    }

    public static boolean isFocused(Object obj) {
        return focused.contains(obj) || focusedByPointer.contains(obj);
    }

    public static void focus(Object obj, boolean replace) {
        if (replace) {
            replaceFocus(obj);
        }

        if (!isFocused(obj)) {
            if (pointerActive) {
                focusedByPointer.add(obj);
            } else {
                focused.add(obj);
            }

            if (obj instanceof FocusAware) {
                ((FocusAware) obj).focused();
            }
        }
    }

    private static boolean removeFocus(Object obj) {
        return focused.remove(obj) || focusedByPointer.remove(obj);
    }

    public static void unfocus(Object obj, boolean recursive) {
        if (removeFocus(obj) && obj instanceof FocusAware) {
            ((FocusAware) obj).unfocused();
        }

        if (recursive && obj instanceof Container) {
            unfocus((Container<?>) obj);
        }
    }

    private static void unfocus(Container<?> container) {
        for (Object item : container.getItems()) {
            unfocus(item, true);
        }
    }

    public static void clearFocus() {
        List<Object> iterationList = new ArrayList<>(FocusManager.focused.size() + focusedByPointer.size());
        iterationList.addAll(focused);
        iterationList.addAll(focusedByPointer);
        for (int i = 0; i < iterationList.size(); ++i) {
            unfocus(iterationList.get(i), false);
        }
    }

    private static void replaceFocus(Object obj) {
        List<Object> iterationList = new ArrayList<>(FocusManager.focused.size() + focusedByPointer.size());
        iterationList.addAll(focused);
        iterationList.addAll(focusedByPointer);
        for (int i = 0; i < iterationList.size(); ++i) {
            Object focusedObject = iterationList.get(i);
            if (focusedObject != obj) {
                unfocus(iterationList.get(i), false);
            }
        }
    }

    static void keyDownEvent(KeyAndPointerState state, int key, boolean repeat) {
        try {
            iterationList.addAll(focused);
            iterationList.addAll(focusedByPointer);
            for (int i = 0; i < iterationList.size(); ++i) {
                Object obj = iterationList.get(i);
                if (obj instanceof KeyDownAware) {
                    ((KeyDownAware) obj).keyDown(state, key, repeat);
                }
            }
        } finally {
            iterationList.clear();
        }
    }

    static void keyUpEvent(KeyAndPointerState state, int key) {
        try {
            iterationList.addAll(focused);
            iterationList.addAll(focusedByPointer);
            for (int i = 0; i < iterationList.size(); ++i) {
                Object obj = iterationList.get(i);
                if (obj instanceof KeyUpAware) {
                    ((KeyUpAware) obj).keyUp(state, key);
                }
            }
        } finally {
            iterationList.clear();
        }
    }

    static void charInputEvent(char charInput) {
        try {
            iterationList.addAll(focused);
            iterationList.addAll(focusedByPointer);
            for (int i = 0; i < iterationList.size(); ++i) {
                Object obj = iterationList.get(i);
                if (obj instanceof InputAware) {
                    ((InputAware) obj).charInput(charInput);
                }
            }
        } finally {
            iterationList.clear();
        }
    }

    private static void onPointerActionFinished(KeyAndPointerState state, int id, int button, boolean released) {
        if (!released) {
            try {
                iterationList.addAll(focused);
                for (int i = 0; i < iterationList.size(); ++i) {
                    Object obj = iterationList.get(i);
                    if (obj instanceof PointerDownWhenFocusedAware) {
                        ((PointerDownWhenFocusedAware) obj).pointerDownWhenFocused(state, id, button);
                    }
                }
            } finally {
                iterationList.clear();
            }
        }
    }

    static void pointerDownFinished(KeyAndPointerState state, int id, int button) {
        onPointerActionFinished(state, id, button, false);
    }

    static void pointerUpFinished(KeyAndPointerState state, int id, int button) {
        onPointerActionFinished(state, id, button, true);
    }
}