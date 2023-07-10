package com.gamelibrary2d.demos.draganddrop;

public interface Draggable {
    boolean onDragStarted(int pointerId, int button);
    boolean onDrag(float deltaX, float deltaY);
    void onDragFinished(int pointerId, int button);
}
