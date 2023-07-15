package com.gamelibrary2d;

public interface KeyAndPointerState {

    boolean isKeyDown(int key);

    boolean isPointerDown(int pointerId);

    boolean isPointerDown(int pointerId, int button);

}
