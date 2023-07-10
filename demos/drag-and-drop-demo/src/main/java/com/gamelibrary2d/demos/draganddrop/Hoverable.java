package com.gamelibrary2d.demos.draganddrop;

public interface Hoverable {
    boolean onHoverStarted(int pointerId);
    boolean onHover();
    void onHoverFinished(int pointerId);
}
