package com.gamelibrary2d.components.frames;

public interface FrameInitializationTask {
    void perform(FrameInitializationContext context) throws Throwable;
}
