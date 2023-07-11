package com.gamelibrary2d;

public interface PointerState {

    boolean isDown(int pointerId);

    boolean isDown(int pointerId, int button);
}
