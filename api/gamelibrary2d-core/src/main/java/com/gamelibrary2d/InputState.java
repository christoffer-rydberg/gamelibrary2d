package com.gamelibrary2d;

public interface InputState {

    boolean isKeyDown(int key);

    boolean isPointerDown(int pointerId);

    boolean isPointerDown(int pointerId, int button);

}
